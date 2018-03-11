package org.metafetish.buttplug.server;

import org.metafetish.buttplug.core.ButtplugEventHandler;

public interface IDeviceSubtypeManager {
    ButtplugEventHandler deviceAdded = new ButtplugEventHandler();

    ButtplugEventHandler scanningFinished = new ButtplugEventHandler();

    void startScanning();

    void stopScanning();

    boolean isScanning();
}
