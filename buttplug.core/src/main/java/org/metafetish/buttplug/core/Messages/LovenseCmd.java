package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LovenseCmd extends ButtplugDeviceMessage {

    @JsonProperty(value="Command", required = true)
    public String deviceCmd;
    
    public LovenseCmd(long deviceIndex, String deviceCmd, long id)
    {
        super(id, deviceIndex);
        this.deviceCmd = deviceCmd;
    }
    
    public LovenseCmd(long deviceIndex, String deviceCmd)
    {
        super(ButtplugConsts.DefaultMsgId, deviceIndex);
        this.deviceCmd = deviceCmd;
    }
}
