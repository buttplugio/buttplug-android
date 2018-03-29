package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class KiirooGen2VibeBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        RxTouch,
        RxAccel
    }

    private List<String> names = new ArrayList<String>() {{
        add("Pearl2");
        add("Fuse");
        add("Virtual Blowbot");
    }};

    public List<String> getNames() {
        return this.names;
    }

    private List<UUID> services = new ArrayList<UUID>() {{
        add(UUID.fromString("88f82580-0000-01e6-aace-0002a5d5c51b"));
    }};

    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<UUID>() {{
        // tx
        add(UUID.fromString("88f82581-0000-01e6-aace-0002a5d5c51b"));
        // rx (touch: 3 zone bitmask)
        add(UUID.fromString("88f82582-0000-01e6-aace-0002a5d5c51b"));
        // rx (accellorometer?)
        add(UUID.fromString("88f82584-0000-01e6-aace-0002a5d5c51b"));
    }};

    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new KiirooGen2Vibe(iface, this);
    }
}
