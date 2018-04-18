package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.LinkedHashMap;

@JsonPropertyOrder({"DeviceName", "DeviceIndex", "DeviceMessages"})
public class DeviceMessageInfo {

    @JsonProperty(value = "DeviceName", required = true)
    public String deviceName;

    @JsonProperty(value = "DeviceIndex", required = true)
    public long deviceIndex;

    @JsonProperty(value = "DeviceMessages", required = true)
    public LinkedHashMap<String, MessageAttributes> deviceMessages;

    public DeviceMessageInfo(DeviceAdded deviceInfo) {
        this(deviceInfo.deviceIndex, deviceInfo.deviceName, deviceInfo.deviceMessages);
    }

    public DeviceMessageInfo(long deviceIndex, String deviceName,
                             LinkedHashMap<String, MessageAttributes> deviceMessages) {
        this.deviceName = deviceName;
        this.deviceIndex = deviceIndex;
        this.deviceMessages = deviceMessages;
    }

    @SuppressWarnings("unused")
    private DeviceMessageInfo() {
        this.deviceName = "";
        this.deviceIndex = -1;
        this.deviceMessages = new LinkedHashMap<>();
    }
}
