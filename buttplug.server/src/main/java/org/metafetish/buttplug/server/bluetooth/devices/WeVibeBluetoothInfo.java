package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;


public class WeVibeBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        Rx
    }

    @SuppressWarnings("SpellCheckingInspection")
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

    @NonNull
    public List<String> getNames() {
        return this.names;
    }

    private List<String> services = new ArrayList<String>() {{
        add("f000bb03-0451-4000-b000-000000000000");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("f000c000-0451-4000-b000-000000000000");
        // rx
        add("f000b000-0451-4000-b000-000000000000");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new WeVibe(iface, this);
    }
}
