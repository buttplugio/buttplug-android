package org.metafetish.buttplug.server.bluetooth;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.server.DeviceSubtypeManager;
import org.metafetish.buttplug.server.bluetooth.devices.FleshlightLaunchBluetoothInfo;

import java.util.ArrayList;
import java.util.List;

public abstract class BluetoothSubtypeManager extends DeviceSubtypeManager {
    protected List<IBluetoothDeviceInfo> builtinDevices;

    protected BluetoothSubtypeManager() {
        builtinDevices = new ArrayList<>();
        builtinDevices.add(new FleshlightLaunchBluetoothInfo());
    }
}