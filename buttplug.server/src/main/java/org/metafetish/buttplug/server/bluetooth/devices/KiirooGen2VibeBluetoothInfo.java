package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;


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

    @NonNull
    public List<String> getNames() {
        return this.names;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> services = new ArrayList<String>() {{
        add("88f82580-0000-01e6-aace-0002a5d5c51b");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("88f82581-0000-01e6-aace-0002a5d5c51b");
        // rx (touch: 3 zone bitmask)
        add("88f82582-0000-01e6-aace-0002a5d5c51b");
        // rx (accellorometer?)
        add("88f82584-0000-01e6-aace-0002a5d5c51b");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new KiirooGen2Vibe(iface, this);
    }
}
