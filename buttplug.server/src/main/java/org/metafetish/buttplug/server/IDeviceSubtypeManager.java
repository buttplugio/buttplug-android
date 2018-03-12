package org.metafetish.buttplug.server;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugEventHandler;

public interface IDeviceSubtypeManager {
    @NonNull
    ButtplugEventHandler getDeviceAdded();

    @NonNull
    ButtplugEventHandler getScanningFinished();

    void startScanning();

    void stopScanning();

    boolean isScanning();
}
