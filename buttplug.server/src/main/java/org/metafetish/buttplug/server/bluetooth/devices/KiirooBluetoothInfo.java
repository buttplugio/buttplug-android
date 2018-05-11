package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;


public class KiirooBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Rx,
        Tx,
    }

    private List<String> names = new ArrayList<String>() {{
        add("ONYX");
        add("PEARL");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> services = new ArrayList<String>() {{
        add("49535343-fe7d-4ae5-8fa9-9fafd205e455");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> characteristics = new ArrayList<String>() {{
        // rx
        add("49535343-1e4d-4bd9-ba61-23c647249616");
        // tx
        add("49535343-8841-43f4-a8d4-ecbe34729bb3");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Kiiroo(iface, this);
    }
}
