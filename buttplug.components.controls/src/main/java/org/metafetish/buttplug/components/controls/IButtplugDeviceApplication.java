package org.metafetish.buttplug.components.controls;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugEventHandler;

public interface IButtplugDeviceApplication {

    @NonNull
    ButtplugEventHandler getStartScanning();

    @NonNull
    ButtplugEventHandler getStopScanning();

    @NonNull
    ButtplugEventHandler getDeviceAdded();

    @NonNull
    ButtplugEventHandler getDeviceRemoved();

    @NonNull
    ButtplugEventHandler getDevicesReset();
}
