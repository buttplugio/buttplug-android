package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class LovenseRev3BluetoothInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx,
        Rx
    }

    private List<String> names = new ArrayList<String>() {{
        // Edge
        add("LVS-P\\d{2}");

        // Domi
        add("LVS-Domi\\d{2}");

        // Hush
        add("LVS-Z\\d{2}");

        // Lush
        add("LVS-S\\d{2}");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }

    private List<String> services = new ArrayList<String>() {{
        add("[a-f0-9]{7}1-[a-f0-9]{4}-4bd4-bbd5-a6920e4c5653");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("[a-f0-9]{7}2-[a-f0-9]{4}-4bd4-bbd5-a6920e4c5653");
        // rx
        add("[a-f0-9]{7}3-[a-f0-9]{4}-4bd4-bbd5-a6920e4c5653");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new Lovense(iface, this);
    }
}
