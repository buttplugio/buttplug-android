package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id"})
public class RequestDeviceList extends ButtplugMessage {

    @SuppressWarnings("unused")
    private RequestDeviceList() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public RequestDeviceList(long id) {
        super(id);
    }
}
