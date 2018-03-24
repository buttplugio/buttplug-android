package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id"})
public class StopScanning extends ButtplugMessage {

    @SuppressWarnings("unused")
    private StopScanning() {
        super(ButtplugConsts.DefaultMsgId);
    }

    public StopScanning(long id) {
        super(id);
    }
}
