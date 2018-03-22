package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
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

    private List<String> names = new ArrayList<String>() {{
        add("Launch");
    }};

    public List<String> getNames() {
        return this.names;
    }

    private List<UUID> services = new ArrayList<UUID>() {{
        add(UUID.fromString("88f80580-0000-01e6-aace-0002a5d5c51b"));
    }};

    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<UUID>() {{
        // tx
        add(UUID.fromString("88f80581-0000-01e6-aace-0002a5d5c51b"));
        // rx
        add(UUID.fromString("88f80582-0000-01e6-aace-0002a5d5c51b"));
        // cmd
        add(UUID.fromString("88f80583-0000-01e6-aace-0002a5d5c51b"));
    }};

    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice CreateDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new FleshlightLaunch(iface, this);
    }
}
