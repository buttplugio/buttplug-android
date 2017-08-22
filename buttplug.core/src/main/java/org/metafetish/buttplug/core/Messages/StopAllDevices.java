package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class StopAllDevices extends ButtplugMessage {

    @SuppressWarnings("unused")
    private StopAllDevices() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public StopAllDevices(long id) {
        super(id);
    }
}
