package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;


public class VorzeA10CycloneInfo implements IBluetoothDeviceInfo {
    public enum Chrs {
        Tx
    }

    private List<String> names = new ArrayList<String>() {{
        add("CycSA");
    }};

    @NonNull
    public List<String> getNames() {
        return this.names;
    }

    private List<String> services = new ArrayList<String>() {{
        add("40ee1111-63ec-4b7f-8ce7-712efd55b90e");
    }};

    @NonNull
    public List<String> getServices() {
        return this.services;
    }

    private List<String> characteristics = new ArrayList<String>() {{
        // tx
        add("40ee2222-63ec-4b7f-8ce7-712efd55b90e");
    }};

    @NonNull
    public List<String> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new VorzeA10Cyclone(iface, this);
    }
}
