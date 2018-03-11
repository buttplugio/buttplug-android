package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.util.ArrayList;
import java.util.List;

public class DeviceList extends ButtplugMessage implements IButtplugMessageOutgoingOnly {

    @JsonProperty(value = "Devices", required = true)
    public List<DeviceMessageInfo> devices;

    public DeviceList(List<DeviceMessageInfo> devices, long id) {
        super(id);
        this.devices = devices;
    }

    @SuppressWarnings("unused")
    private DeviceList() {
        super(ButtplugConsts.DefaultMsgId);
        this.devices = new ArrayList<>();
    }
}