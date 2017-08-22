package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class Ping extends ButtplugMessage {

    @SuppressWarnings("unused")
    private Ping() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public Ping(long id) {
        super(id);
    }
}
