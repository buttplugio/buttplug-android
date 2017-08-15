package org.metafetish.buttplug.client;

import org.metafetish.buttplug.core.Messages.DeviceAdded;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.DeviceRemoved;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ButtplugClientDevice {
    public long index;

    public String name;

    public List<String> allowedMessages;

    public ButtplugClientDevice(DeviceMessageInfo aDevInfo) {
        index = aDevInfo.deviceIndex;
        name = aDevInfo.deviceName;
        allowedMessages = Arrays.asList(aDevInfo.deviceMessages);
    }

    public ButtplugClientDevice(DeviceAdded aDevInfo) {
        index = aDevInfo.deviceIndex;
        name = aDevInfo.deviceName;
        allowedMessages = Arrays.asList(aDevInfo.deviceMessages);
    }

    public ButtplugClientDevice(DeviceRemoved aDevInfo) {
        index = aDevInfo.deviceIndex;
        name = "";
        allowedMessages = new ArrayList<>();
    }
}
