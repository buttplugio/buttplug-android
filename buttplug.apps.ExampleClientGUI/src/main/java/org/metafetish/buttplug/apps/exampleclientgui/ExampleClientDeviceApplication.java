package org.metafetish.buttplug.apps.exampleclientgui;

import android.app.Application;
import android.support.annotation.NonNull;

import org.metafetish.buttplug.components.controls.IButtplugDeviceApplication;
import org.metafetish.buttplug.core.ButtplugEventHandler;

public class ExampleClientDeviceApplication extends Application implements IButtplugDeviceApplication {
    private ButtplugEventHandler startScanning = new ButtplugEventHandler();
    private ButtplugEventHandler stopScanning = new ButtplugEventHandler();
    private ButtplugEventHandler deviceAdded = new ButtplugEventHandler();
    private ButtplugEventHandler deviceRemoved = new ButtplugEventHandler();
    private ButtplugEventHandler devicesReset = new ButtplugEventHandler();

    @NonNull
    @Override
    public ButtplugEventHandler getStartScanning() {
        return this.startScanning;
    }

    @NonNull
    @Override
    public ButtplugEventHandler getStopScanning() {
        return this.stopScanning;
    }

    @NonNull
    @Override
    public ButtplugEventHandler getDeviceAdded() {
        return this.deviceAdded;
    }

    @NonNull
    @Override
    public ButtplugEventHandler getDeviceRemoved() {
        return this.deviceRemoved;
    }

    @NonNull
    @Override
    public ButtplugEventHandler getDevicesReset() {
        return this.devicesReset;
    }
}
