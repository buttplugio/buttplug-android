package org.metafetish.buttplug.components.controls;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugEventHandler;

public interface IButtplugDeviceApplication {

    @NonNull
    public ButtplugEventHandler getStartScanning();

    @NonNull
    public ButtplugEventHandler getStopScanning();

    @NonNull
    public ButtplugEventHandler getDeviceAdded();

    @NonNull
    public ButtplugEventHandler getDeviceRemoved();

    @NonNull
    public ButtplugEventHandler getDevicesReset();
}
