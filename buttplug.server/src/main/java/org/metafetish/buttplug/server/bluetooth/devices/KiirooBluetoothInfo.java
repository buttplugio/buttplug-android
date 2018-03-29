package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class KiirooBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Rx,
        Tx,
    }

    private List<String> names = new ArrayList<String>() {{
        add("ONYX");
        add("PEARL");
    }};

    public List<String> getNames() {
        return this.names;
    }

    private List<UUID> services = new ArrayList<UUID>() {{
        add(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"));
    }};

    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<UUID>() {{
        // rx
        add(UUID.fromString("49535343-1e4d-4bd9-ba61-23c647249616"));
        // tx
        add(UUID.fromString("49535343-8841-43f4-a8d4-ecbe34729bb3"));
    }};

    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Kiiroo(iface, this);
    }
}
