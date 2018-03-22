package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class LovenseRev5BluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        Rx
    }

    private List<String> names = new ArrayList<String>() {{
        // Hush. Again.
        add("LVS-Z36");
    }};

    public List<String> getNames() {
        return this.names;
    }

    private List<UUID> services = new ArrayList<UUID>() {{
        add(UUID.fromString("5a300001-0024-4bd4-bbd5-a6920e4c5653"));
    }};

    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<UUID>() {{
        // tx
        add(UUID.fromString("5a300002-0024-4bd4-bbd5-a6920e4c5653"));
        // rx
        add(UUID.fromString("5a300003-0024-4bd4-bbd5-a6920e4c5653"));
    }};

    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice CreateDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Lovense(iface, this);
    }
}
