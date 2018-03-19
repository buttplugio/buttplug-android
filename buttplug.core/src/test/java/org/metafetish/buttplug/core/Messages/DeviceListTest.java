package org.metafetish.buttplug.core.Messages;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class DeviceListTest {

    @Test
    public void test() throws IOException {
        String testStr = "[" +
                "{" +
                "\"DeviceList\":{" +
                "\"Id\":1," +
                "\"Devices\":[" +
                "{" +
                "\"DeviceIndex\":0," +
                "\"DeviceName\":\"TestDevice 1\"," +
                "\"DeviceMessages\":{" +
                "\"SingleMotorVibrateCmd\":{}," +
                "\"VibrateCmd\":{\"FeatureCount\":2}," +
                "\"StopDeviceCmd\":{}" +
                "}" +
                "}," +
                "{" +
                "\"DeviceIndex\":1," +
                "\"DeviceName\":\"TestDevice 2\"," +
                "\"DeviceMessages\":{" +
                "\"FleshlightLaunchFW12Cmd\":{}," +
                "\"LinearCmd\":{\"FeatureCount\":1}," +
                "\"StopDeviceCmd\":{}" +
                "}" +
                "}" +
                "]" +
                "}" +
                "}" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.parseJson(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(DeviceList.class, msgs.get(0).getClass());
        Assert.assertEquals(1, msgs.get(0).id);
        Assert.assertEquals(2, ((DeviceList) msgs.get(0)).devices.size());

        List<DeviceMessageInfo> devs = ((DeviceList) msgs.get(0)).devices;

        Assert.assertEquals(0, devs.get(0).deviceIndex);
        Assert.assertEquals("TestDevice 1", devs.get(0).deviceName);
        Assert.assertEquals(3, devs.get(0).deviceMessages.size());

        Assert.assertNotNull(devs.get(0).deviceMessages.get("SingleMotorVibrateCmd"));
        Assert.assertEquals(0, devs.get(0).deviceMessages.get("SingleMotorVibrateCmd")
                .featureCount);
        Assert.assertNotNull(devs.get(0).deviceMessages.get("VibrateCmd"));
        Assert.assertEquals(2, devs.get(0).deviceMessages.get("VibrateCmd").featureCount);
        Assert.assertNotNull(devs.get(0).deviceMessages.get("StopDeviceCmd"));
        Assert.assertEquals(0, devs.get(0).deviceMessages.get("StopDeviceCmd").featureCount);

        Assert.assertEquals(1, devs.get(1).deviceIndex);
        Assert.assertEquals("TestDevice 2", devs.get(1).deviceName);
        Assert.assertEquals(3, devs.get(1).deviceMessages.size());

        Assert.assertNotNull(devs.get(1).deviceMessages.get("FleshlightLaunchFW12Cmd"));
        Assert.assertEquals(0, devs.get(1).deviceMessages.get("FleshlightLaunchFW12Cmd")
                .featureCount);
        Assert.assertNotNull(devs.get(1).deviceMessages.get("LinearCmd"));
        Assert.assertEquals(1, devs.get(1).deviceMessages.get("LinearCmd").featureCount);
        Assert.assertNotNull(devs.get(1).deviceMessages.get("StopDeviceCmd"));
        Assert.assertEquals(0, devs.get(1).deviceMessages.get("StopDeviceCmd").featureCount);

        String jsonOut = parser.formatJson(msgs);
        Assert.assertEquals(testStr, jsonOut);

        jsonOut = parser.formatJson(msgs.get(0));
        Assert.assertEquals(testStr, jsonOut);
    }

}
