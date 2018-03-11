package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.HashMap;
import java.util.Map;

public class DeviceAdded extends ButtplugDeviceMessage implements IButtplugMessageOutgoingOnly {
    @JsonProperty(value = "DeviceName", required = true)
    public String deviceName;

    @JsonProperty(value = "DeviceMessages", required = true)
    public Map<String, MessageAttributes> deviceMessages;

    public DeviceAdded(long deviceIndex, String deviceName, Map<String, MessageAttributes>
            deviceMessages) {
        super(ButtplugConsts.SystemMsgId, deviceIndex);

        this.deviceName = deviceName;
        this.deviceMessages = deviceMessages;
    }

    @SuppressWarnings("unused")
    private DeviceAdded() {
        super(ButtplugConsts.SystemMsgId, 0);
        this.deviceName = "";
        this.deviceMessages = new HashMap<>();
    }
}
