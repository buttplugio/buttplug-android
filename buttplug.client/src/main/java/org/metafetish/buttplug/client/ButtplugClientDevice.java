package org.metafetish.buttplug.client;

import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.Messages.DeviceAdded;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.DeviceRemoved;

import java.util.ArrayList;
import java.util.List;

public class ButtplugClientDevice {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

    public long index;

    public String name;

    public List<String> allowedMessages;

    public ButtplugClientDevice(DeviceMessageInfo deviceInfo) {
        index = deviceInfo.deviceIndex;
        name = deviceInfo.deviceName;
//        allowedMessages = Arrays.asList(deviceInfo.deviceMessages);
    }

    public ButtplugClientDevice(DeviceAdded deviceInfo) {
        index = deviceInfo.deviceIndex;
        name = deviceInfo.deviceName;
//        allowedMessages = Arrays.asList(deviceInfo.deviceMessages);
    }

    public ButtplugClientDevice(DeviceRemoved deviceInfo) {
        index = deviceInfo.deviceIndex;
        name = "";
        allowedMessages = new ArrayList<>();
    }
}
