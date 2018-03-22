package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class WeVibeBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        Rx
    }

    private List<String> names = new ArrayList<String>() {{
        add("Cougar");
        add("4 Plus");
        add("4plus");
        add("Bloom");
        add("classic");
        add("Ditto");
        add("Gala");
        add("Jive");
        add("Nova");
        add("NOVAV2");
        add("Pivot");
        add("Rave");
        add("Sync");
        add("Verge");
        add("Wish");
    }};

    public List<String> getNames() {
        return this.names;
    }

    private List<UUID> services = new ArrayList<UUID>() {{
        add(UUID.fromString("f000bb03-0451-4000-b000-000000000000"));
    }};

    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<UUID>() {{
        // tx
        add(UUID.fromString("f000c000-0451-4000-b000-000000000000"));
        // rx
        add(UUID.fromString("f000b000-0451-4000-b000-000000000000"));
    }};

    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice CreateDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new WeVibe(iface, this);
    }
}
