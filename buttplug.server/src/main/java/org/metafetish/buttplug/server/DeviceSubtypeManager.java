package org.metafetish.buttplug.server;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.IButtplugLogManager;

public abstract class DeviceSubtypeManager implements IDeviceSubtypeManager {
    @NonNull
    protected IButtplugLogManager bpLogManager = new ButtplugLogManager();

    @NonNull
    protected IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass().getSimpleName());

    private ButtplugEventHandler deviceAdded = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getDeviceAdded() {
        return this.deviceAdded;
    }

    private ButtplugEventHandler scanningFinished = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getScanningFinished() {
        return this.scanningFinished;
    }

    protected DeviceSubtypeManager() {
        this.bpLogger.debug(String.format("Setting up Device Manager %s", this.getClass().getSimpleName()));
    }

    public abstract void startScanning();

    public abstract void stopScanning();

    public abstract boolean isScanning();
}
