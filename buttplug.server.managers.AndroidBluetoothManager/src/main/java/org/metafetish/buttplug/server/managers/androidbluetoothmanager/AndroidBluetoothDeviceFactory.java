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
    ButtplugEventHandler deviceCreated = new ButtplugEventHandler();

    public AndroidBluetoothDeviceFactory(@NonNull Activity aActivity,
                                         @NonNull IButtplugLogManager aLogManager,
                                         @NonNull IBluetoothDeviceInfo aInfo) {
        Log.d(TAG, "ainfo: " + aInfo.getClass().getSimpleName());
        this.activity = aActivity;
        this.bpLogManager = aLogManager;
        this.bpLogger = this.bpLogManager.getLogger(this.getClass());
        this.bpLogger.trace("Creating " + this.getClass().getSimpleName());
        this.deviceInfo = aInfo;
    }

    public boolean mayBeDevice(String advertName, List<UUID> advertGUIDs) {
        if (!deviceInfo.names.isEmpty() && !deviceInfo.names.contains(advertName)) {
            return false;
        }

        Log.d(TAG, "Found " + advertName + " for " + deviceInfo.getClass().getSimpleName());

        if (!deviceInfo.names.isEmpty() && advertGUIDs.isEmpty()) {
            Log.d(TAG, "No advertised services?");
            return true;
        }

        //TODO: Print debug info
        if (advertGUIDs.containsAll(deviceInfo.services)) {
            return true;
        }
        return false;
    }

    // TODO Have this throw exceptions instead of return null. Once we've made it this far, if we
    // don't find what we're expecting, that's weird.
    // [ItemCanBeNull]
    void createDeviceAsync(@NonNull BluetoothDevice aDevice) throws ExecutionException,
            InterruptedException {
        // TODO This assumes we're always planning on having the UUIDs sorted in the Info
        // classes, which is probably not true.
        AndroidBluetoothDeviceInterface bleInterface = new AndroidBluetoothDeviceInterface(this.activity, this.bpLogManager, aDevice, this.deviceInfo);
        bleInterfaces.put(aDevice.getAddress(), bleInterface);
        bleInterface.deviceConnected.addCallback(new IButtplugCallback() {
            @Override
            public void invoke(ButtplugEvent aEvent) {
                String btAddr = aEvent.getString();
                Log.d(TAG, "Device connected (" + btAddr + ")");
                if (btAddr == null) {
                    return;
                }
                AndroidBluetoothDeviceInterface bleInterface = bleInterfaces.get(btAddr);
                if (bleInterface == null) {
                    return;
                }

                BluetoothDevice aDevice = bleInterface.getDevice();
//                String btAddr = aDevice.getAddress();
                List<BluetoothGattService> services = bleInterface.getServices();
                if (services.isEmpty()) {
                    Log.d(TAG, "No services found for " + aDevice.getName());
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                List<UUID> serviceUuids = new ArrayList<>();
                for (BluetoothGattService service : services) {
                    Log.d(TAG, "Found service UUID: " + service.getUuid().toString() + " (" +
                            aDevice.getName() + ")");
                    serviceUuids.add(service.getUuid());
                }

                Log.d(TAG, "deviceInfo is " + deviceInfo.getClass().getSimpleName());
                if (!serviceUuids.containsAll(deviceInfo.services)) {
                    Log.d(TAG, "Cannot find service for device (" + aDevice.getName() + ")");
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                Log.d(TAG, "services type is " + AndroidBluetoothDeviceFactory.this.deviceInfo.services.getClass().getSimpleName());
                Log.d(TAG, "services size: " + AndroidBluetoothDeviceFactory.this.deviceInfo.services.size());
                Log.d(TAG, "names size: " + AndroidBluetoothDeviceFactory.this.deviceInfo.names.size());
                if (deviceInfo.services.size() == 0) {
                    Log.d(TAG, "WTF0 (" + aDevice.getName() + ")");
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                }
                UUID firstUuid = deviceInfo.services.get(0);
                int serviceUuidIndex = serviceUuids.indexOf(firstUuid);
                if (serviceUuidIndex == -1) {
                    Log.d(TAG, "WTF1 (" + aDevice.getName() + ")");
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                } else if (serviceUuidIndex >= services.size()) {
                    Log.d(TAG, "WTF2 (" + aDevice.getName() + ")");
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                }
                BluetoothGattService service = services.get(serviceUuidIndex);

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                if (characteristics.isEmpty()) {
                    Log.d(TAG, "No characteristics found for " + aDevice.getName());
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                List<UUID> characteristicUuids = new ArrayList<>();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d(TAG, "Found characteristic UUID: " + characteristic.getUuid().toString
                            () + " (" + aDevice.getName() + ")");
                    characteristicUuids.add(characteristic.getUuid());
                }

                if (!characteristicUuids.containsAll(deviceInfo.characteristics)) {
                    Log.d(TAG, "Cannot find characteristic for device (" + aDevice.getName() + ")");
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                Map<UUID, BluetoothGattCharacteristic> gattCharacteristics = new HashMap<>();
                for (UUID uuid : deviceInfo.characteristics) {
                    gattCharacteristics.put(uuid, characteristics.get(characteristicUuids.indexOf
                            (uuid)));
                }
                bleInterface.setGattCharacteristics(gattCharacteristics);

                IButtplugDevice device = deviceInfo.CreateDevice(AndroidBluetoothDeviceFactory.this.bpLogManager, bleInterface);
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
