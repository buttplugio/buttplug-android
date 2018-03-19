package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;

public class DeviceMessageInfo {

    @JsonProperty(value = "DeviceIndex", required = true)
    public long deviceIndex;

    @JsonProperty(value = "DeviceName", required = true)
    public String deviceName;

    @JsonProperty(value = "DeviceMessages", required = true)
    public LinkedHashMap<String, MessageAttributes> deviceMessages;

    public DeviceMessageInfo(long deviceIndex, String deviceName, LinkedHashMap<String,
            MessageAttributes>
            deviceMessages) {
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
