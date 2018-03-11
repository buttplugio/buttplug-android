package org.metafetish.buttplug.server.bluetooth.devices;

import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.springframework.util.concurrent.ListenableFuture;

public interface ILinearCmd {
    ListenableFuture<ButtplugMessage> handleLinearCmd(ButtplugDeviceMessage aMsg);
}
