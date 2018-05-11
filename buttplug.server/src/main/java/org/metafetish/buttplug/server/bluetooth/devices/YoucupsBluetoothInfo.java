package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;


public class YoucupsBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx
    }

    private List<String> names = new ArrayList<String>() {{
        // Warrior II
        add("Youcups");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }


    private List<String> services = new ArrayList<String>() {{
        add("0000fee9-0000-1000-8000-00805f9b34fb");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("d44bc439-abfd-45a2-b575-925416129600");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Youcups(iface, this);
    }
}
