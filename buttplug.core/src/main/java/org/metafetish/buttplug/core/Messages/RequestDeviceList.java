package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class RequestDeviceList extends ButtplugMessage {

    @SuppressWarnings("unused")
    private RequestDeviceList() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public RequestDeviceList(long id) {
        super(id);
    }
}
