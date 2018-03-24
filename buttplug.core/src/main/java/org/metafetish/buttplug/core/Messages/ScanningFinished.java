package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id"})
public class ScanningFinished extends ButtplugMessage implements IButtplugMessageOutgoingOnly {

    public ScanningFinished() {
        super(ButtplugConsts.SystemMsgId);
    }
}
