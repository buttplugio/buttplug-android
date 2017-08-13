package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class ScanningFinished extends ButtplugMessage {

    public ScanningFinished() {
        super(ButtplugConsts.SystemMsgId);
    }
}
