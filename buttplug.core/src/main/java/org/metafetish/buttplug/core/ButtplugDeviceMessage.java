package org.metafetish.buttplug.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ButtplugDeviceMessage extends ButtplugMessage {

    @JsonProperty(value = "DeviceIndex", required = true)
    public long deviceIndex;

    public ButtplugDeviceMessage(long id, long deviceIndex) {
        this(id, deviceIndex, 0);
    }

    public ButtplugDeviceMessage(long id, long deviceIndex, long aSchemaVersion) {
        super(id, aSchemaVersion);
        this.deviceIndex = deviceIndex;
    }
}
