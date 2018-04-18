package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class LovenseRev2BluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        Rx
    }

    private List<String> names = new ArrayList<String>() {{
        // Lush
        add("LVS-S001");
        // Hush
        add("LVS-Z001");
        add("LVS_Z001");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }

    private List<UUID> services = new ArrayList<UUID>() {{
        add(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"));
    }};

    @NonNull
    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<UUID>() {{
        // tx
        add(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"));
        // rx
        add(UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"));
    }};

    @NonNull
    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Lovense(iface, this);
    }
}
