package org.metafetish.buttplug.core.Messages;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class DeviceAddedTest {

    @Test
    public void test() throws IOException {
        String testStr = "[" +
                "{" +
                "\"DeviceAdded\":{" +
                "\"Id\":0," +
                "\"DeviceIndex\":0," +
                "\"DeviceName\":\"TestDevice 1\"," +
                "\"DeviceMessages\":{" +
                "\"SingleMotorVibrateCmd\":{}," +
                "\"VibrateCmd\":{\"FeatureCount\":2}," +
                "\"StopDeviceCmd\":{}" +
                "}" +
                "}" +
                "}" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.parseJson(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(DeviceAdded.class, msgs.get(0).getClass());
        Assert.assertEquals(0, msgs.get(0).id);
        Assert.assertEquals(0, ((DeviceAdded) msgs.get(0)).deviceIndex);
        Assert.assertEquals("TestDevice 1", ((DeviceAdded) msgs.get(0)).deviceName);
        LinkedHashMap<String, MessageAttributes> deviceMessages = ((DeviceAdded) msgs.get(0))
                .deviceMessages;
        Assert.assertNotNull(deviceMessages);
        Assert.assertEquals(3, deviceMessages.size());
        Assert.assertNotNull(deviceMessages.get("SingleMotorVibrateCmd"));
        Assert.assertEquals(0, deviceMessages.get("SingleMotorVibrateCmd").featureCount);
        Assert.assertNotNull(deviceMessages.get("VibrateCmd"));
        Assert.assertEquals(2, deviceMessages.get("VibrateCmd").featureCount);
        Assert.assertNotNull(deviceMessages.get("StopDeviceCmd"));
        Assert.assertEquals(0, deviceMessages.get("StopDeviceCmd").featureCount);

        String jsonOut = parser.formatJson(msgs);
        Assert.assertEquals(testStr, jsonOut);

        jsonOut = parser.formatJson(msgs.get(0));
        Assert.assertEquals(testStr, jsonOut);
    }

}
