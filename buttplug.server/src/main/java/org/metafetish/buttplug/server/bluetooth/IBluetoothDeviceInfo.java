package org.metafetish.buttplug.server.bluetooth;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;

import java.util.List;

public interface IBluetoothDeviceInfo {
    @NonNull
    List<String> getNames();

    @NonNull
    List<String> getServices();

    @NonNull
    List<String> getCharacteristics();

    @NonNull
    IButtplugDevice createDevice(@NonNull IBluetoothDeviceInterface deviceInterface);
}
