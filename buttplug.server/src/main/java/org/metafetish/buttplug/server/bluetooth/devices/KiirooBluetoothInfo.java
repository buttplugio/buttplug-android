package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.core.IButtplugLogManager;
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

    private List<String> names = new ArrayList<>();

    public List<String> getNames() {
        return this.names;
    }

    private List<UUID> services = new ArrayList<>();

    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<>();

    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    public KiirooBluetoothInfo() {
        names.add("ONYX");
        names.add("PEARL");
        services.add(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"));
        // rx
        characteristics.add(UUID.fromString("49535343-1e4d-4bd9-ba61-23c647249616"));
        // tx
        characteristics.add(UUID.fromString("49535343-8841-43f4-a8d4-ecbe34729bb3"));
    }

    @NonNull
    public IButtplugDevice CreateDevice(@NonNull IButtplugLogManager logManager, @NonNull
            IBluetoothDeviceInterface iface) {
        return new FleshlightLaunch(logManager, iface, this);
    }
}
