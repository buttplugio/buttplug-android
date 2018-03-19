package org.metafetish.buttplug.server;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.core.Messages.DeviceAdded;
import org.metafetish.buttplug.core.Messages.DeviceList;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.DeviceRemoved;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.RequestDeviceList;
import org.metafetish.buttplug.core.Messages.StartScanning;
import org.metafetish.buttplug.core.Messages.StopAllDevices;
import org.metafetish.buttplug.core.Messages.StopDeviceCmd;
import org.metafetish.buttplug.core.Messages.StopScanning;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class DeviceManager {

    private final Object scanLock;
    private ButtplugEventHandler deviceMessageReceived = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getDeviceMessageReceived() {
        return this.deviceMessageReceived;
    }

    private ButtplugEventHandler scanningFinished = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getScanningFinished() {
        return this.scanningFinished;
    }

    private List<IDeviceSubtypeManager> managers;
    private Map<Long, IButtplugDevice> devices;
    private IButtplugLog bpLogger;
    private IButtplugLogManager bpLogManager;
    private AtomicLong deviceIndexCounter;
    private boolean sentFinished;
    private IButtplugCallback messageEmittedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            if (deviceMessageReceived != null) {
                deviceMessageReceived.invoke(event);
            }
        }
    };
    private IButtplugCallback deviceRemovedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            IButtplugDevice device = event.getDevice();
            if (device == null) {
                DeviceManager.this.bpLogger.error("Got DeviceRemoved message from an object that " +
                        "is not a ButtplugDevice.");
                return;
            }

            // The device itself will fire the remove event, so look it up in the dictionary and
            // translate that for clients.
            Map<Long, IButtplugDevice> entries = new HashMap<>();
            for (Map.Entry<Long, IButtplugDevice> x : DeviceManager.this.devices.entrySet()) {
                if (x.getValue().getIdentifier().equals(device.getIdentifier())) {
                    entries.put(x.getKey(), x.getValue());
                }
            }
            if (entries.isEmpty()) {
                DeviceManager.this.bpLogger.error("Got DeviceRemoved Event from object that is " +
                        "not in devices dictionary");
            } else if (entries.size() > 1) {
                DeviceManager.this.bpLogger.error("Device being removed has multiple entries in " +
                        "device dictionary.");
            }
            for (Map.Entry<Long, IButtplugDevice> x : entries.entrySet()) {
                x.getValue().getDeviceRemoved().removeCallback(DeviceManager.this
                        .deviceRemovedCallback);
//                x.getValue().getMessageEmitted().removeCallback(DeviceManager.this
// .messageEmittedCallback);
                DeviceManager.this.deviceMessageReceived.invoke(new ButtplugEvent(new
                        DeviceRemoved(x.getKey())));
            }
        }
    };
    private IButtplugCallback deviceAddedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            IButtplugDevice device = event.getDevice();

            // Devices can be turned off by the time they get to this point, at which point they
            // end up null. Make sure the device isn't null.
            if (device == null) {
                return;
            }

            @SuppressLint("UseSparseArrays")
            Map<Long, IButtplugDevice> duplicates = new HashMap<>();
            for (Map.Entry<Long, IButtplugDevice> x : DeviceManager.this.devices.entrySet()) {
                if (x.getValue().getIdentifier().equals(device.getIdentifier())) {
                    duplicates.put(x.getKey(), x.getValue());
                }
            }
            if (!duplicates.isEmpty() && (duplicates.size() > 1 || duplicates.entrySet().iterator
                    ().next().getValue().isConnected())) {
                DeviceManager.this.bpLogger.debug("Already have device " + device.getName() + " " +
                        "in Devices list");
                return;
            }

            // If we get to 4 billion devices connected, this may be a problem.
            long deviceIndex = !duplicates.isEmpty() ? duplicates.entrySet().iterator().next()
                    .getKey() : DeviceManager.this.deviceIndexCounter.incrementAndGet();
            DeviceManager.this.bpLogger.info((!duplicates.isEmpty() ? "Readding" : "Adding") + " " +
                    "Device " + device.getName() + " at index " + deviceIndex);

            DeviceManager.this.devices.put(deviceIndex, device);
            DeviceManager.this.devices.get(deviceIndex).setIndex(deviceIndex);
            device.getDeviceRemoved().addCallback(DeviceManager.this.deviceRemovedCallback);
            device.getMessageEmitted().addCallback(DeviceManager.this.messageEmittedCallback);
            ButtplugMessage msg = new DeviceAdded(
                    deviceIndex,
                    device.getName(),
                    DeviceManager.getAllowedMessageTypesAsDictionary(device)
            );
            if (deviceMessageReceived != null) {
                deviceMessageReceived.invoke(new ButtplugEvent(msg));
            }
        }
    };
    private IButtplugCallback scanningFinishedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            synchronized (DeviceManager.this.scanLock) {
                if (DeviceManager.this.sentFinished) {
                    return;
                }

                boolean done = true;
                for (IDeviceSubtypeManager mgr : DeviceManager.this.managers) {
                    done &= !mgr.isScanning();
                }
                if (!done) {
                    return;
                }
                DeviceManager.this.sentFinished = true;
                DeviceManager.this.scanningFinished.invoke(new ButtplugEvent());
            }
        }
    };

    public DeviceManager(IButtplugLogManager logManager) {
        this.bpLogManager = logManager;
        this.bpLogger = this.bpLogManager.getLogger(this.getClass());
        this.bpLogger.info("Setting up DeviceManager");
        this.sentFinished = true;
        this.devices = new ConcurrentHashMap<>();
        this.deviceIndexCounter = new AtomicLong(0);
        this.scanLock = new Object();
        this.managers = new ArrayList<>();
    }

    private static LinkedHashMap<String, MessageAttributes> getAllowedMessageTypesAsDictionary
            (@NonNull IButtplugDevice device) {
        LinkedHashMap<String, MessageAttributes> msgs = new LinkedHashMap<>();
        for (Class msg : device.getAllowedMessageTypes()) {
            msgs.put(msg.getSimpleName(), device.getMessageAttrs(msg));
        }

        return msgs;
    }

    public Map<Long, IButtplugDevice> getDevices() {
        return this.devices;
    }

    protected ListenableFuture<ButtplugMessage> sendMessage(ButtplugMessage msg)
            throws ExecutionException, InterruptedException, InvocationTargetException,
            IllegalAccessException {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();

        long id = msg.id;
        if (msg instanceof StartScanning) {
            DeviceManager.this.bpLogger.debug("Got StartScanning Message");
            DeviceManager.this.startScanning();
            promise.set(new Ok(id));
            return promise;
        } else if (msg instanceof StopScanning) {
            DeviceManager.this.bpLogger.debug("Got StopScanning Message");
            DeviceManager.this.stopScanning();
            promise.set(new Ok(id));
            return promise;
        } else if (msg instanceof StopAllDevices) {
            DeviceManager.this.bpLogger.debug("Got StopAllDevices Message");
            boolean isOk = true;
            String errorMsg = "";
            for (Map.Entry<Long, IButtplugDevice> device : this.devices.entrySet()) {
                if (!device.getValue().isConnected()) {
                    continue;
                }
                ButtplugMessage r = device.getValue().parseMessage(new StopDeviceCmd(device
                        .getKey(), msg.id)).get();
                if (r instanceof Ok) {
                    continue;
                }
                isOk = false;
                errorMsg = errorMsg.concat(((Error) r).errorMessage + "; ");
            }
            if (isOk) {
                promise.set(new Ok(id));
                return promise;
            }

            promise.set(new Error(errorMsg, Error.ErrorClass.ERROR_DEVICE, msg.id));
            return promise;
        } else if (msg instanceof RequestDeviceList) {
            DeviceManager.this.bpLogger.debug("Got RequestDeviceList Message");
            ArrayList<DeviceMessageInfo> msgDevices = new ArrayList<>();
            for (Map.Entry<Long, IButtplugDevice> x : DeviceManager.this.devices.entrySet()) {
                if (x.getValue().isConnected()) {
                    msgDevices.add(new DeviceMessageInfo(
                            x.getKey(),
                            x.getValue().getName(),
                            DeviceManager.getAllowedMessageTypesAsDictionary(x.getValue())
                    ));
                }
            }
            promise.set(new DeviceList(msgDevices, id));
            return promise;
        } else if (msg instanceof ButtplugDeviceMessage) {
            DeviceManager.this.bpLogger.trace("Sending " + msg.getClass().getSimpleName() + " to" +
                    " device index " + ((ButtplugDeviceMessage) msg).deviceIndex);
            // If it's a device message, it's most likely not ours.
            ButtplugDeviceMessage deviceMessage = (ButtplugDeviceMessage) msg;
            if (this.devices.containsKey(deviceMessage.deviceIndex)) {
                ButtplugMessage result = DeviceManager.this.devices.get(deviceMessage.deviceIndex)
                        .parseMessage(deviceMessage).get();
                promise.set(result);
                return promise;
            }
            promise.set(DeviceManager.this.bpLogger.logErrorMsg(id, Error.ErrorClass
                    .ERROR_DEVICE, "Dropping message for unknown device index " + deviceMessage
                    .deviceIndex));
            return promise;
        }
        promise.set(
                new Error("Message type " + msg.getClass().getSimpleName() + " unhandled by this" +
                        " server.",
                        Error.ErrorClass.ERROR_MSG, id));
        return promise;
    }

    void removeAllDevices() {
        this.stopScanning();
        Collection<IButtplugDevice> removeDevices = this.devices.values();
        this.devices.clear();
        for (IButtplugDevice removeDevice : removeDevices) {
            removeDevice.getDeviceRemoved().removeCallback(this.deviceRemovedCallback);
            removeDevice.disconnect();
        }
    }

    private void startScanning() {
        synchronized (this.scanLock) {
            this.sentFinished = false;
            for (IDeviceSubtypeManager mgr : this.managers) {
                mgr.startScanning();
            }
        }
    }

    void stopScanning() {
        for (IDeviceSubtypeManager mgr : this.managers) {
            mgr.stopScanning();
        }
    }

    void addDeviceSubtypeManager(IDeviceSubtypeManager mgr) {
        this.managers.add(mgr);
        mgr.getDeviceAdded().addCallback(this.deviceAddedCallback);
        mgr.getScanningFinished().addCallback(this.scanningFinishedCallback);
    }
}
