package org.metafetish.buttplug.core;

public interface IButtplugDeviceMessageCallback {
    abstract ButtplugMessage invoke(ButtplugDeviceMessage msg);
}
