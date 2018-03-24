package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id"})
public class Ok extends ButtplugMessage implements IButtplugMessageOutgoingOnly {

    public Ok(long id) {
        super(id);
    }

    @SuppressWarnings("unused")
    private Ok() {
        super(ButtplugConsts.DefaultMsgId);
    }
}
