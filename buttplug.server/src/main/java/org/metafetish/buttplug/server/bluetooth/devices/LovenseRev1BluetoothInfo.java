package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;

public class LovenseRev1BluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        Rx
    }

    private List<String> names = new ArrayList<String>() {{
        // Nora
        add("LVS-A011");
        add("LVS-C011");
        // Max
        add("LVS-B011");
        // Ambi
        add("LVS-L009");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }

    private List<String> services = new ArrayList<String>() {{
        add("0000fff0-0000-1000-8000-00805f9b34fb");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("0000fff2-0000-1000-8000-00805f9b34fb");
        // rx
        add("0000fff1-0000-1000-8000-00805f9b34fb");
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
