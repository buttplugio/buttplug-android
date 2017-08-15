package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

public class StopDeviceCmd extends ButtplugDeviceMessage
{
    public StopDeviceCmd(long deviceIndex)
    { 
        super(ButtplugConsts.DefaultMsgId, deviceIndex);
    }

    public StopDeviceCmd(long deviceIndex, long id) {
        super(id, deviceIndex);
    }

    private StopDeviceCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
    }
}
