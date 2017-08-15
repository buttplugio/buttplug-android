package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

public class DeviceRemoved extends ButtplugDeviceMessage
{
    public DeviceRemoved(long deviceMessage)
    { 
        super(ButtplugConsts.SystemMsgId, deviceMessage);
    }

    private DeviceRemoved() {
        super(ButtplugConsts.SystemMsgId, -1);
    }
}
