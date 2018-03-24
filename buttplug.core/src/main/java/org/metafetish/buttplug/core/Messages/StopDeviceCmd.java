package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

@JsonPropertyOrder({"Id", "DeviceIndex"})
public class StopDeviceCmd extends ButtplugDeviceMessage {

    public StopDeviceCmd(long deviceIndex, long id) {
        super(id, deviceIndex);
    }

    @SuppressWarnings("unused")
    private StopDeviceCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
    }
}
