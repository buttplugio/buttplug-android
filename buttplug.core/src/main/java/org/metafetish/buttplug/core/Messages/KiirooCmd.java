package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

public class KiirooCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Command", required = true)
    public String deviceCmd;

    public KiirooCmd(long deviceIndex, String deviceCmd, long id) {
        super(id, deviceIndex);
        this.deviceCmd = deviceCmd;
    }

    @SuppressWarnings("unused")
    private KiirooCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        this.deviceCmd = "";
    }
}
