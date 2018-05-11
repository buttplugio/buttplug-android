package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;

public class FleshlightLaunchBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        Rx,
        Cmd
    }

    private List<String> names = new ArrayList<String>() {{
        add("Launch");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> services = new ArrayList<String>() {{
        add("88f80580-0000-01e6-aace-0002a5d5c51b");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("88f80581-0000-01e6-aace-0002a5d5c51b");
        // rx
        add("88f80582-0000-01e6-aace-0002a5d5c51b");
        // cmd
        add("88f80583-0000-01e6-aace-0002a5d5c51b");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new FleshlightLaunch(iface, this);
    }
}
