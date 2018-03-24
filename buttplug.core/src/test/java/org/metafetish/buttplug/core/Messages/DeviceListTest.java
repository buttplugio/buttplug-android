package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class DeviceListTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"DeviceList\": {\n" +
                "      \"Id\": 1,\n" +
                "      \"Devices\": [\n" +
                "        {\n" +
                "          \"DeviceName\": \"TestDevice 1\",\n" +
                "          \"DeviceIndex\": 0,\n" +
                "          \"DeviceMessages\": {\n" +
                "            \"SingleMotorVibrateCmd\": {},\n" +
                "            \"VibrateCmd\": { \"FeatureCount\": 2 },\n" +
                "            \"StopDeviceCmd\": {}\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"DeviceName\": \"TestDevice 2\",\n" +
                "          \"DeviceIndex\": 1,\n" +
                "          \"DeviceMessages\": {\n" +
                "            \"FleshlightLaunchFW12Cmd\": {},\n" +
                "            \"LinearCmd\": { \"FeatureCount\": 1 },\n" +
                "            \"StopDeviceCmd\": {}\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(DeviceList.class, msgs.get(0).getClass());
        Assert.assertEquals(1, msgs.get(0).id);
        Assert.assertEquals(2, ((DeviceList) msgs.get(0)).devices.size());

        List<DeviceMessageInfo> devs = ((DeviceList) msgs.get(0)).devices;

        Assert.assertEquals(0, devs.get(0).deviceIndex);
        Assert.assertEquals("TestDevice 1", devs.get(0).deviceName);
        Assert.assertEquals(3, devs.get(0).deviceMessages.size());
        Assert.assertEquals(0, devs.get(0).deviceMessages.get("SingleMotorVibrateCmd").featureCount);
        Assert.assertEquals(2, devs.get(0).deviceMessages.get("VibrateCmd").featureCount);
        Assert.assertEquals(0, devs.get(0).deviceMessages.get("StopDeviceCmd").featureCount);

        Assert.assertEquals(1, devs.get(1).deviceIndex);
        Assert.assertEquals("TestDevice 2", devs.get(1).deviceName);
        Assert.assertEquals(3, devs.get(1).deviceMessages.size());
        Assert.assertEquals(0, devs.get(1).deviceMessages.get("FleshlightLaunchFW12Cmd").featureCount);
        Assert.assertEquals(1, devs.get(1).deviceMessages.get("LinearCmd").featureCount);
        Assert.assertEquals(0, devs.get(1).deviceMessages.get("StopDeviceCmd").featureCount);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 1);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 1);
        Assert.assertEquals(uglyStr, jsonOut);
    }

}
