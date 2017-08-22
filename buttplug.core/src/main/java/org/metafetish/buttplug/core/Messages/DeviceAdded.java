package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

public class DeviceAdded extends ButtplugDeviceMessage {
    @JsonProperty(value = "DeviceName", required = true)
    public String deviceName;

    @JsonProperty(value = "DeviceMessages", required = true)
    public String[] deviceMessages;

    public DeviceAdded(long deviceIndex, String deviceName, String[] deviceMessages) {
        super(ButtplugConsts.SystemMsgId, deviceIndex);

        this.deviceName = deviceName;
        this.deviceMessages = deviceMessages;
    }

    @SuppressWarnings("unused")
    private DeviceAdded() {
        super(ButtplugConsts.SystemMsgId, 0);
        this.deviceName = "";
        this.deviceMessages = new String[]{};
    }
}
