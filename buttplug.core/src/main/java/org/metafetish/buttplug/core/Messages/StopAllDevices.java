package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id"})
public class StopAllDevices extends ButtplugMessage {

    public StopAllDevices() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public StopAllDevices(long id) {
        super(id);
    }
}
