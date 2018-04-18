package org.metafetish.buttplug.client;

import com.google.common.util.concurrent.SettableFuture;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugLogLevel;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.Messages.DeviceAdded;
import org.metafetish.buttplug.core.Messages.DeviceList;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.DeviceRemoved;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.Log;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.RequestDeviceList;
import org.metafetish.buttplug.core.Messages.RequestLog;
import org.metafetish.buttplug.core.Messages.RequestServerInfo;
import org.metafetish.buttplug.core.Messages.ServerInfo;
import org.metafetish.buttplug.core.Messages.StartScanning;
import org.metafetish.buttplug.core.Messages.StopAllDevices;
import org.metafetish.buttplug.core.Messages.StopScanning;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ButtplugClient {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    protected IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass().getSimpleName());

    //TODO: Migrate these to .Events subclasses?
    public ButtplugEventHandler deviceAdded = new ButtplugEventHandler();
    public ButtplugEventHandler deviceRemoved = new ButtplugEventHandler();
//    public ButtplugEventHandler scanningFinished = new ButtplugEventHandler();
    public ButtplugEventHandler initialized = new ButtplugEventHandler();
    public ButtplugEventHandler errorReceived = new ButtplugEventHandler();

    protected ButtplugJsonMessageParser parser;
    private Object sendLock = new Object();
    private String clientName;
    protected ConcurrentHashMap<Long, Future<ButtplugMessage>> waitingMsgs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, DeviceMessageInfo> devices = new ConcurrentHashMap<>();
    private long messageSchemaVersion;
    protected boolean connecting;
    protected boolean connected;

    protected AtomicLong msgId = new AtomicLong(1);

    public ButtplugClient(String clientName) {
        this.clientName = clientName;
        this.parser = new ButtplugJsonMessageParser();
        this.bpLogger.info("Finished setting up ButtplugClient");
        this.messageSchemaVersion = ButtplugMessage.currentSchemaVersion;
    }

    public long getNextMsgId() {
        return msgId.getAndIncrement();
    }

    public void connect() throws Exception {
        if (this.connecting || this.connected) {
            throw new IllegalStateException("Already connected!");
        }
        this.connecting = true;

        this.waitingMsgs.clear();
        this.devices.clear();
        this.msgId.set(1);
    }

    public void onServerInfo(ServerInfo msg) throws Exception {
        requestDeviceList();
    }

    public void onConnect() throws Exception {
        this.connecting = false;
        this.connected = true;
        requestServerInfo();
    }

    public void disconnect() {
        if (this.connected) {
            this.connected = false;
            int max = 3;
            while (max-- > 0 && this.waitingMsgs.size() != 0) {
                for (long msgId : this.waitingMsgs.keySet()) {
                    SettableFuture<ButtplugMessage> val = (SettableFuture<ButtplugMessage>) this.waitingMsgs.remove(msgId);
                    if (val != null) {
                        val.set(new Error("Connection closed!", Error.ErrorClass.ERROR_UNKNOWN,
                                ButtplugConsts.SystemMsgId));
                    }
                }
            }
            msgId.set(1);
        }
    }

    public void onMessage(ButtplugMessage msg) {
        if (msg.id > 0) {
            SettableFuture<ButtplugMessage> val = (SettableFuture<ButtplugMessage>) this.waitingMsgs.remove(msg.id);
            if (val != null) {
                val.set(msg);
                return;
            }
        }

        if (msg instanceof Log) {
            this.bpLogger.getLogMessageReceived().invoke(new ButtplugEvent(msg));
        } else if (msg instanceof DeviceAdded) {
            this.bpLogger.debug("got DeviceAdded");
            DeviceMessageInfo device = new DeviceMessageInfo((DeviceAdded) msg);
            this.devices.put(((DeviceAdded) msg).deviceIndex, device);
            this.deviceAdded.invoke(new ButtplugEvent(msg));
        } else if (msg instanceof DeviceRemoved) {
            this.bpLogger.debug("got DeviceRemoved");
            if (this.devices.remove(((DeviceRemoved) msg).deviceIndex) != null) {
                this.deviceRemoved.invoke(new ButtplugEvent(msg));
            }
            //TODO: Do we need this?
//                } else if (msg instanceof ScanningFinished) {
//                    this.scanningFinished.invoke(new ButtplugEvent(msg));
        } else if (msg instanceof Error) {
            this.errorReceived.invoke(new ButtplugEvent(msg));
        }
    }

    public void onMessage(String buf) {
        try {
            List<ButtplugMessage> msgs = this.parser.deserialize(buf);

            for (ButtplugMessage msg : msgs) {
                this.onMessage(msg);
            }
        } catch (IOException e) {
            this.errorReceived.invoke(new ButtplugEvent(new Error(e.getMessage(),
                    Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.SystemMsgId)));
        }
    }

    void onError(Exception ex) {
        this.bpLogger.warn("onError");
        this.connecting = false;
        this.errorReceived.invoke(new ButtplugEvent(new Error(ex.getMessage(), Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.SystemMsgId)));
    }

    private void requestServerInfo() throws Exception {
        ButtplugMessage msg = new RequestServerInfo(this.clientName);
        ButtplugMessage res = sendMessage(msg).get();
        if (res instanceof ServerInfo) {
            this.bpLogger.debug("got ServerInfo");
            this.onServerInfo((ServerInfo) res);
        } else if (res instanceof Error) {
            //TODO: Replace all generic exceptions with specific ones
            throw new Exception(((Error) res).errorMessage);
        } else {
            throw new Exception(String.format("Unexpected message returned: %s", res.getClass().getName()));
        }
    }

    public void requestDeviceList() throws Exception {
        ButtplugMessage msg = new RequestDeviceList(msgId.incrementAndGet());
        ButtplugMessage res = sendMessage(msg).get();
        if (res instanceof DeviceList) {
            if (((DeviceList) res).devices != null) {
                for (DeviceMessageInfo deviceInfo : ((DeviceList) res).devices) {
                    if (!this.devices.containsKey(deviceInfo.deviceIndex)) {
                        if (this.devices.put(deviceInfo.deviceIndex, deviceInfo) == null) {
                            this.deviceAdded.invoke(new ButtplugEvent(new DeviceAdded(deviceInfo)));
                        }
                    }
                }
            }
            this.initialized.invoke(new ButtplugEvent());
        } else if (res instanceof Error) {
            throw new Exception(((Error) res).errorMessage);
        } else {
            throw new Exception(String.format("Unexpected message returned: %s", res.getClass().getName()));
        }
    }

    public Map<Long, DeviceMessageInfo> getDevices() {
        return this.devices;
    }

    public boolean startScanning() throws ExecutionException, InterruptedException {
        return sendMessageExpectOk(new StartScanning(msgId.incrementAndGet()));
    }

    public boolean stopScanning() throws ExecutionException, InterruptedException {
        return sendMessageExpectOk(new StopScanning(msgId.incrementAndGet()));
    }

    public boolean stopAllDevices() throws ExecutionException, InterruptedException {
        return sendMessageExpectOk(new StopAllDevices(msgId.incrementAndGet()));
    }

    public boolean requestLog(ButtplugLogLevel logLevel)
            throws ExecutionException, InterruptedException {
        return sendMessageExpectOk(new RequestLog(logLevel, msgId.getAndIncrement()));
    }

    public Future<ButtplugMessage> sendDeviceMessage(final long deviceIndex, final ButtplugDeviceMessage deviceMsg) {
        final SettableFuture<ButtplugMessage> promise = SettableFuture.create();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                DeviceMessageInfo deviceInfo = ButtplugClient.this.devices.get(deviceIndex);
                if (deviceInfo != null) {
                    if (!deviceInfo.deviceMessages.keySet().contains(deviceMsg.getClass().getSimpleName())) {
                        promise.set(new Error(String.format("Device does not accept message type: %s",
                                deviceMsg.getClass().getSimpleName()),
                                Error.ErrorClass.ERROR_DEVICE,
                                ButtplugConsts.SystemMsgId));
                    } else {
                        deviceMsg.id = ButtplugClient.this.msgId.incrementAndGet();
                        deviceMsg.deviceIndex = deviceIndex;
                        try {
                            promise.set(sendMessage(deviceMsg).get());
                        } catch (InterruptedException | ExecutionException e) {
                            promise.set(new Error(e.getMessage(), Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.DefaultMsgId));
                        }
                    }
                } else {
                    promise.set(new Error("Device not available.", Error.ErrorClass.ERROR_DEVICE,
                            ButtplugConsts.SystemMsgId));
                }
            }
        });
        return promise;
    }

    protected boolean sendMessageExpectOk(ButtplugMessage msg)
            throws ExecutionException, InterruptedException {
        return sendMessage(msg).get() instanceof Ok;
    }

    //TODO: Automatically getAndIncrement this.msgId
    protected abstract Future<ButtplugMessage> sendMessage(ButtplugMessage msg);
}
