package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;


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

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> services = new ArrayList<String>() {{
        add("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
        // rx
        add("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Lovense(iface, this);
    }
}
