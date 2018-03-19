package org.metafetish.buttplug.server.managers.androidbluetoothmanager;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.util.Log;

import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AndroidBluetoothDeviceFactory {
    private static final String TAG = AndroidBluetoothManager.class.getSimpleName();
    private Activity activity;

    @NonNull
    private IButtplugLog bpLogger;

    @NonNull
    public IBluetoothDeviceInfo deviceInfo;

    @NonNull
    private IButtplugLogManager bpLogManager;

    private Map<String, AndroidBluetoothDeviceInterface> bleInterfaces = new HashMap<>();
    private ButtplugEventHandler deviceCreated = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getDeviceCreated() {
        return this.deviceCreated;
    }

    public AndroidBluetoothDeviceFactory(@NonNull Activity activity,
                                         @NonNull IButtplugLogManager logManager,
                                         @NonNull IBluetoothDeviceInfo info) {
        this.activity = activity;
        this.bpLogManager = logManager;
        this.bpLogger = this.bpLogManager.getLogger(this.getClass());
        this.bpLogger.trace("Creating " + this.getClass().getSimpleName());
        this.deviceInfo = info;
    }

    public boolean mayBeDevice(String advertName, List<UUID> advertGUIDs) {
        if (!deviceInfo.getNames().isEmpty() && !deviceInfo.getNames().contains(advertName)) {
            return false;
        }

        Log.d(TAG, "Found " + advertName + " for " + deviceInfo.getClass().getSimpleName());

        if (!deviceInfo.getNames().isEmpty() && advertGUIDs.isEmpty()) {
            Log.d(TAG, "No advertised services?");
            return true;
        }

        //TODO: Print debug info
        if (advertGUIDs.containsAll(deviceInfo.getServices())) {
            return true;
        }
        return false;
    }

    // TODO Have this throw exceptions instead of return null. Once we've made it this far, if we
    // don't find what we're expecting, that's weird.
    // [ItemCanBeNull]
    void createDeviceAsync(@NonNull BluetoothDevice device) throws ExecutionException,
            InterruptedException {
        // TODO This assumes we're always planning on having the UUIDs sorted in the Info
        // classes, which is probably not true.
        AndroidBluetoothDeviceInterface bleInterface = new AndroidBluetoothDeviceInterface(this
                .activity, this.bpLogManager, device, this.deviceInfo);
        bleInterfaces.put(device.getAddress(), bleInterface);
        bleInterface.getDeviceConnected().addCallback(new IButtplugCallback() {
            @Override
            public void invoke(ButtplugEvent event) {
                String btAddr = event.getString();
                Log.d(TAG, "Device connected (" + btAddr + ")");
                if (btAddr == null) {
                    return;
                }
                AndroidBluetoothDeviceInterface bleInterface = bleInterfaces.get(btAddr);
                if (bleInterface == null) {
                    return;
                }

                BluetoothDevice bluetoothDevice = bleInterface.getDevice();
//                String btAddr = bluetoothDevice.getAddress();
                List<BluetoothGattService> services = bleInterface.getServices();
                if (services.isEmpty()) {
                    Log.d(TAG, "No services found for " + bluetoothDevice.getName());
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                List<UUID> serviceUuids = new ArrayList<>();
                for (BluetoothGattService service : services) {
                    Log.d(TAG, "Found service UUID: " + service.getUuid().toString() + " (" +
                            bluetoothDevice.getName() + ")");
                    serviceUuids.add(service.getUuid());
                }

                if (!serviceUuids.containsAll(deviceInfo.getServices())) {
                    Log.d(TAG, "Cannot find service for device (" + bluetoothDevice.getName() +
                            ")");
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                if (deviceInfo.getServices().size() == 0) {
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                }

                UUID firstUuid = deviceInfo.getServices().get(0);
                int serviceUuidIndex = serviceUuids.indexOf(firstUuid);
                BluetoothGattService service = services.get(serviceUuidIndex);

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                if (characteristics.isEmpty()) {
                    Log.d(TAG, "No characteristics found for " + bluetoothDevice.getName());
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                List<UUID> characteristicUuids = new ArrayList<>();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d(TAG, "Found characteristic UUID: " + characteristic.getUuid().toString
                            () + " (" + bluetoothDevice.getName() + ")");
                    characteristicUuids.add(characteristic.getUuid());
                }

                if (!characteristicUuids.containsAll(deviceInfo.getCharacteristics())) {
                    Log.d(TAG, "Cannot find characteristic for device (" + bluetoothDevice
                            .getName() + ")");
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                Map<UUID, BluetoothGattCharacteristic> gattCharacteristics = new HashMap<>();
                for (UUID uuid : deviceInfo.getCharacteristics()) {
                    gattCharacteristics.put(uuid, characteristics.get(characteristicUuids.indexOf
                            (uuid)));
                }
                bleInterface.setGattCharacteristics(gattCharacteristics);

                IButtplugDevice device = deviceInfo.CreateDevice(AndroidBluetoothDeviceFactory
                        .this.bpLogManager, bleInterface);
                ButtplugMessage msg = null;
                try {
                    msg = device.initialize().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (msg instanceof Ok) {
                    deviceCreated.invoke(new ButtplugEvent(device));
                    return;
                }
                // If initialization fails, don't actually send the message back. Just return
                // null, we'll have the info in the logs.
                deviceCreated.invoke(new ButtplugEvent(btAddr));
            }
        });
    }
}
