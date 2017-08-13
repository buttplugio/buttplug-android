package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceList extends ButtplugMessage {
    
    @JsonProperty(value = "Devices", required = true)
    public DeviceMessageInfo[] devices;

    public DeviceList(DeviceMessageInfo[] devices) {
        super(ButtplugConsts.DefaultMsgId);
        this.devices = devices;
    }

    public DeviceList(DeviceMessageInfo[] devices, long id) {
        super(id);
        this.devices = devices;
    }

    private DeviceList() {
        super(ButtplugConsts.DefaultMsgId);
        this.devices = new DeviceMessageInfo[] {};
    }
}