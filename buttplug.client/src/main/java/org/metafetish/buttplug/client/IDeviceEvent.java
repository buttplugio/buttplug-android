package org.metafetish.buttplug.client;

public interface IDeviceEvent {
    void deviceAdded(ButtplugClientDevice dev);

    void deviceRemoved(long index);
}
