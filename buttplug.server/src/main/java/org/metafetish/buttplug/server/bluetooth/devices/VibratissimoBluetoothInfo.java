package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VibratissimoBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        TxMode,
        TxSpeed,
        Rx
    }

    private List<String> names = new ArrayList<String>() {{
        add("Vibratissimo");
    }};

    public List<String> getNames() {
        return this.names;
    }


    private List<UUID> services = new ArrayList<UUID>() {{
        add(UUID.fromString("00001523-1212-efde-1523-785feabcd123"));
    }};

    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<UUID>() {{
        // tx (mode)
        add(UUID.fromString("00001524-1212-efde-1523-785feabcd123"));
        // tx (speed)
        add(UUID.fromString("00001526-1212-efde-1523-785feabcd123"));
        // rx
        add(UUID.fromString("00001527-1212-efde-1523-785feabcd123"));
    }};

    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Vibratissimo(iface, this);
    }
}
