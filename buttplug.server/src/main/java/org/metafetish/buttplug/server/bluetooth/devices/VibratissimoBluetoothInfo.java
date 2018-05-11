package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;

public class VibratissimoBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        TxMode,
        TxSpeed,
        Rx
    }

    private List<String> names = new ArrayList<String>() {{
        add("Vibratissimo");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }


    @SuppressWarnings("SpellCheckingInspection")
    private List<String> services = new ArrayList<String>() {{
        add("00001523-1212-efde-1523-785feabcd123");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> characteristics = new ArrayList<String>() {{
        // tx (mode)
        add("00001524-1212-efde-1523-785feabcd123");
        // tx (speed)
        add("00001526-1212-efde-1523-785feabcd123");
        // rx
        add("00001527-1212-efde-1523-785feabcd123");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Vibratissimo(iface, this);
    }
}
