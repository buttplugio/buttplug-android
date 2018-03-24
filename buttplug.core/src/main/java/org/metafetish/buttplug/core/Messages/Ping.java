package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id"})
public class Ping extends ButtplugMessage {

    @SuppressWarnings("unused")
    private Ping() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public Ping(long id) {
        super(id);
    }
}
