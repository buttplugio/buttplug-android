package org.metafetish.buttplug.server.managers.androidbluetoothmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugLogManager;
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

class AndroidBluetoothDeviceFactory {
    private Context context;

    @NonNull
    private IButtplugLogManager bpLogManager = new ButtplugLogManager();

    @NonNull
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

    @NonNull
    private IBluetoothDeviceInfo deviceInfo;

    private Map<String, AndroidBluetoothDeviceInterface> bleInterfaces = new HashMap<>();
    private ButtplugEventHandler deviceCreated = new ButtplugEventHandler();

    @NonNull
    ButtplugEventHandler getDeviceCreated() {
        return this.deviceCreated;
    }

    AndroidBluetoothDeviceFactory(@NonNull Context context,
                                         @NonNull IBluetoothDeviceInfo info) {
        this.context = context;
        this.bpLogger.trace(String.format("Creating %s", this.getClass().getSimpleName()));
        this.deviceInfo = info;
    }

    boolean mayBeDevice(String advertName, List<UUID> advertGUIDs) {
        if (!deviceInfo.getNames().isEmpty() && !deviceInfo.getNames().contains(advertName)) {
            return false;
        }

        this.bpLogger.trace(String.format("Found %s for %s", advertName, deviceInfo.getClass().getSimpleName()));

        if (!deviceInfo.getNames().isEmpty() && advertGUIDs.isEmpty()) {
            this.bpLogger.trace("No advertised services?");
            return true;
        }

        //TODO: Print debug info
        return advertGUIDs.containsAll(deviceInfo.getServices());
    }

    // TODO Have this throw exceptions instead of return null.
    // Once we've made it this far, if we don't find what we're expecting, that's weird.
    // [ItemCanBeNull]
    void createDeviceAsync(@NonNull BluetoothDevice device) {
        // TODO This assumes we're always planning on having the UUIDs sorted in the Info classes
        // which is probably not true.
        AndroidBluetoothDeviceInterface bleInterface = new AndroidBluetoothDeviceInterface(
                this.context, device);
        bleInterfaces.put(device.getAddress(), bleInterface);
        bleInterface.getDeviceConnected().addCallback(new IButtplugCallback() {
            @Override
            public void invoke(ButtplugEvent event) {
                String btAddr = event.getString();
                AndroidBluetoothDeviceFactory.this.bpLogger.trace(String.format("Device connected (%s)", btAddr));
                if (btAddr == null) {
                    return;
                }
                AndroidBluetoothDeviceInterface bleInterface = AndroidBluetoothDeviceFactory.this.bleInterfaces.get(btAddr);
                if (bleInterface == null) {
                    return;
                }

                BluetoothDevice bluetoothDevice = bleInterface.getDevice();
//                String btAddr = bluetoothDevice.getAddress();
                List<BluetoothGattService> services = bleInterface.getServices();
                if (services.isEmpty()) {
                    AndroidBluetoothDeviceFactory.this.bpLogger.trace(
                            String.format("No services found for %s", bluetoothDevice.getName()));
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                List<UUID> serviceUuids = new ArrayList<>();
                for (BluetoothGattService service : services) {
                    AndroidBluetoothDeviceFactory.this.bpLogger.trace(
                            String.format("Found service UUID: %s (%s)",
                                    service.getUuid().toString(),
                                    bluetoothDevice.getName()));
                    serviceUuids.add(service.getUuid());
                }

                if (!serviceUuids.containsAll(deviceInfo.getServices())) {
                    AndroidBluetoothDeviceFactory.this.bpLogger.trace(
                            String.format("Cannot find service for device (%s)",
                                    bluetoothDevice.getName()));
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
                    AndroidBluetoothDeviceFactory.this.bpLogger.trace(
                            String.format("No characteristics found for %s", bluetoothDevice.getName()));
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                List<UUID> characteristicUuids = new ArrayList<>();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    AndroidBluetoothDeviceFactory.this.bpLogger.trace(
                            String.format("Found characteristic UUID: %s (%s)",
                                characteristic.getUuid().toString(),
                                bluetoothDevice.getName()));
                    characteristicUuids.add(characteristic.getUuid());
                }

                if (!characteristicUuids.containsAll(deviceInfo.getCharacteristics())) {
                    AndroidBluetoothDeviceFactory.this.bpLogger.trace(
                            String.format("Cannot find characteristic for device (%s)",
                                    bluetoothDevice.getName()));
                    deviceCreated.invoke(new ButtplugEvent(btAddr));
                    return;
                }

                Map<UUID, BluetoothGattCharacteristic> gattCharacteristics = new HashMap<>();
                for (UUID uuid : deviceInfo.getCharacteristics()) {
                    gattCharacteristics.put(uuid, characteristics.get(characteristicUuids.indexOf
                            (uuid)));
                }
                bleInterface.setGattCharacteristics(gattCharacteristics);

                IButtplugDevice device = deviceInfo.createDevice(bleInterface);
                ButtplugMessage msg = null;
                try {
                    msg = device.initialize().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (msg instanceof Ok) {
                    AndroidBluetoothDeviceFactory.this.deviceCreated.invoke(new ButtplugEvent(device));
                    return;
                }
                // If initialization fails, don't actually send the message back. Just return
                // null, we'll have the info in the logs.
                AndroidBluetoothDeviceFactory.this.deviceCreated.invoke(new ButtplugEvent(btAddr));
            }
        });
    }
}
