package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.LinkedHashMap;

@JsonPropertyOrder({"Id", "DeviceName", "DeviceIndex", "DeviceMessages"})
public class DeviceAdded extends ButtplugDeviceMessage implements IButtplugMessageOutgoingOnly {

    @JsonProperty(value = "DeviceName", required = true)
    public String deviceName;

    @JsonProperty(value = "DeviceMessages", required = true)
    public LinkedHashMap<String, MessageAttributes> deviceMessages;

    public DeviceAdded(DeviceMessageInfo deviceInfo) {
        this(deviceInfo.deviceIndex, deviceInfo.deviceName, deviceInfo.deviceMessages);
    }

    public DeviceAdded(long deviceIndex, String deviceName,
                       LinkedHashMap<String, MessageAttributes> deviceMessages) {
        super(ButtplugConsts.SystemMsgId, deviceIndex);

        this.deviceName = deviceName;
        this.deviceMessages = deviceMessages;
    }

    @SuppressWarnings("unused")
    private DeviceAdded() {
        super(ButtplugConsts.SystemMsgId, 0);
        this.deviceName = "";
        this.deviceMessages = new LinkedHashMap<>();
    }
}
