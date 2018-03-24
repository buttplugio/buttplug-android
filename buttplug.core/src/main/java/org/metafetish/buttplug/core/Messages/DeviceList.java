package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.util.ArrayList;

@JsonPropertyOrder({"Id", "Devices"})
public class DeviceList extends ButtplugMessage implements IButtplugMessageOutgoingOnly {

    @JsonProperty(value = "Devices", required = true)
    public ArrayList<DeviceMessageInfo> devices;

    public DeviceList(ArrayList<DeviceMessageInfo> devices, long id) {
        super(id);
        this.devices = devices;
    }

    @SuppressWarnings("unused")
    private DeviceList() {
        super(ButtplugConsts.DefaultMsgId);
        this.devices = new ArrayList<>();
    }
}