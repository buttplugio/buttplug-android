package org.metafetish.buttplug.server;

//import android.support.annotation.Nullable;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.IButtplugLogManager;

public abstract class DeviceSubtypeManager implements IDeviceSubtypeManager {
    @NonNull
    protected IButtplugLog bpLogger;
    @NonNull
    protected IButtplugLogManager bpLogManager;

    public ButtplugEventHandler deviceAdded;

    public ButtplugEventHandler scanningFinished;

    protected DeviceSubtypeManager(@NonNull IButtplugLogManager aLogManager) {
        this.bpLogManager = aLogManager;
        this.bpLogger = this.bpLogManager.getLogger(this.getClass());
        this.bpLogger.debug("Setting up Device Manager " + this.getClass().getSimpleName());
    }

    public abstract void startScanning();

    public abstract void stopScanning();

    public abstract boolean isScanning();
}
