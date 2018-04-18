package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

    private List<UUID> services = new ArrayList<UUID>() {{
        add(UUID.fromString("40ee1111-63ec-4b7f-8ce7-712efd55b90e"));
    }};

    @NonNull
    public List<UUID> getServices() {
        return this.services;
    }

    private List<UUID> characteristics = new ArrayList<UUID>() {{
        // tx
        add(UUID.fromString("40ee2222-63ec-4b7f-8ce7-712efd55b90e"));
    }};

    @NonNull
    public List<UUID> getCharacteristics() {
        return this.characteristics;
    }

    @NonNull
    public IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface iface) {
        return new VorzeA10Cyclone(iface, this);
    }
}
