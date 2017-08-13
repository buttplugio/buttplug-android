package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class Ok extends ButtplugMessage {

    public Ok() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public Ok(long id) {
        super(id);
    }
}
