package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.ArrayList;

@JsonPropertyOrder({"Id", "DeviceIndex", "Command"})
public class RawCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Command", required = true)
    public ArrayList<Byte> command;

    public RawCmd(long deviceIndex, ArrayList<Byte> command, long id) {
        super(id, deviceIndex);
        this.command = command;
    }

    @SuppressWarnings("unused")
    private RawCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        this.command = null;
    }
}
