package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;


public class MagicMotionBluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
    }

    private List<String> names = new ArrayList<String>() {{
        add("Smart Mini Vibe");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }


    /*
     * This has 6 services! Not sure what does what yet
     *
     * 78667579-7b48-43db-b8c5-7928a6b0a335  // Magic Motion's primary
     * 00001800-0000-1000-8000-00805f9b34fb
     * 00001801-0000-1000-8000-00805f9b34fb
     * 3d3cbc0e-f76b-11e3-8fcd-b2227cce2b54  // Unknown service
     * 0000180f-0000-1000-8000-00805f9b34fb
     * 0000180a-0000-1000-8000-00805f9b34fb
     */
    private List<String> services = new ArrayList<String>() {{
        add("78667579-7b48-43db-b8c5-7928a6b0a335");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("78667579-a914-49a4-8333-aa3c0cd8fedc");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new MagicMotion(iface, this);
    }
}
