package org.metafetish.buttplug.server.bluetooth;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.core.IButtplugLogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface IBluetoothDeviceInfo {
    @NonNull
    List<String> names = new ArrayList<>();

    @NonNull
    List<UUID> services = new ArrayList<>();

    @NonNull
    List<UUID> characteristics = new ArrayList<>();

    @NonNull
    IButtplugDevice CreateDevice(@NonNull IButtplugLogManager aLogManager, @NonNull IBluetoothDeviceInterface aDeviceInterface);
}
