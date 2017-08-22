package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

public class LovenseCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Command", required = true)
    public String deviceCmd;

    public LovenseCmd(long deviceIndex, String deviceCmd, long id) {
        super(id, deviceIndex);
        this.deviceCmd = deviceCmd;
    }

    @SuppressWarnings("unused")
    private LovenseCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        this.deviceCmd = "";
    }
}
