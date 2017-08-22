package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

public class DeviceRemoved extends ButtplugDeviceMessage {
    public DeviceRemoved(long deviceMessage) {
        super(ButtplugConsts.SystemMsgId, deviceMessage);
    }

    @SuppressWarnings("unused")
    private DeviceRemoved() {
        super(ButtplugConsts.SystemMsgId, -1);
    }
}
