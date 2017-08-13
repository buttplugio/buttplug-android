package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class RequestDeviceList extends ButtplugMessage {

    public RequestDeviceList() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public RequestDeviceList(long id) {
        super(id);
    }
}
