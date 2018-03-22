package org.metafetish.buttplug.server.bluetooth;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.server.DeviceSubtypeManager;
import org.metafetish.buttplug.server.bluetooth.devices.FleshlightLaunchBluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.KiirooBluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.KiirooGen2VibeBluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.LovenseRev1BluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.LovenseRev2BluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.LovenseRev3BluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.LovenseRev4BluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.LovenseRev5BluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.LovenseRev6BluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.LovenseRev7BluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.MagicMotionBluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.VibratissimoBluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.VorzeA10CycloneInfo;
import org.metafetish.buttplug.server.bluetooth.devices.WeVibeBluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.YoucupsBluetoothInfo;

import java.util.ArrayList;
import java.util.List;

public abstract class BluetoothSubtypeManager extends DeviceSubtypeManager {
    protected List<IBluetoothDeviceInfo> builtinDevices;

    protected BluetoothSubtypeManager() {
        builtinDevices = new ArrayList<>();
        builtinDevices.add(new FleshlightLaunchBluetoothInfo());
        builtinDevices.add(new KiirooBluetoothInfo());
        builtinDevices.add(new KiirooGen2VibeBluetoothInfo());
        builtinDevices.add(new YoucupsBluetoothInfo());
        builtinDevices.add(new LovenseRev1BluetoothInfo());
        builtinDevices.add(new LovenseRev2BluetoothInfo());
        builtinDevices.add(new LovenseRev3BluetoothInfo());
        builtinDevices.add(new LovenseRev4BluetoothInfo());
        builtinDevices.add(new LovenseRev5BluetoothInfo());
        builtinDevices.add(new LovenseRev6BluetoothInfo());
        builtinDevices.add(new LovenseRev7BluetoothInfo());
        builtinDevices.add(new MagicMotionBluetoothInfo());
        builtinDevices.add(new VibratissimoBluetoothInfo());
        builtinDevices.add(new VorzeA10CycloneInfo());
        builtinDevices.add(new WeVibeBluetoothInfo());
    }
}