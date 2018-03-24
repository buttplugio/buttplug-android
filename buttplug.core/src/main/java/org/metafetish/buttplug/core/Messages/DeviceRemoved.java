package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

@JsonPropertyOrder({"Id", "DeviceIndex"})
public class DeviceRemoved extends ButtplugDeviceMessage implements IButtplugMessageOutgoingOnly {

    public DeviceRemoved(long deviceMessage) {
        super(ButtplugConsts.SystemMsgId, deviceMessage);
    }

    @SuppressWarnings("unused")
    private DeviceRemoved() {
        super(ButtplugConsts.SystemMsgId, -1);
    }
}
