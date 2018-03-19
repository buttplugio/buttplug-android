package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FleshlightLaunchBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        Rx,
        Cmd
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

    public FleshlightLaunchBluetoothInfo() {
        names.add("Launch");
        services.add(UUID.fromString("88f80580-0000-01e6-aace-0002a5d5c51b"));
        // tx
        characteristics.add(UUID.fromString("88f80581-0000-01e6-aace-0002a5d5c51b"));
        // rx
        characteristics.add(UUID.fromString("88f80582-0000-01e6-aace-0002a5d5c51b"));
        // cmd
        characteristics.add(UUID.fromString("88f80583-0000-01e6-aace-0002a5d5c51b"));
    }

    @NonNull
    public IButtplugDevice CreateDevice(@NonNull IButtplugLogManager logManager, @NonNull
            IBluetoothDeviceInterface iface) {
        return new FleshlightLaunch(logManager, iface, this);
    }
}
