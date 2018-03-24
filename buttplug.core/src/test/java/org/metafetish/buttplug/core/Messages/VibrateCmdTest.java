package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class VibrateCmdTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"VibrateCmd\": {\n" +
                "      \"Id\": 1,\n" +
                "      \"DeviceIndex\": 0,\n" +
                "      \"Speeds\": [\n" +
                "        {\n" +
                "          \"Index\": 0,\n" +
                "          \"Speed\": 0.5\n" +
                "        },\n" +
                "        {\n" +
                "          \"Index\": 1,\n" +
                "          \"Speed\": 1.0\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(VibrateCmd.class, msgs.get(0).getClass());
        Assert.assertEquals(1, msgs.get(0).id);
        Assert.assertEquals(0, ((VibrateCmd) msgs.get(0)).deviceIndex);
        Assert.assertEquals(2, ((VibrateCmd) msgs.get(0)).speeds.size());
        Assert.assertEquals(0, ((VibrateCmd) msgs.get(0)).speeds.get(0).index);
        Assert.assertEquals(0.5, ((VibrateCmd) msgs.get(0)).speeds.get(0).getSpeed(), 0);
        Assert.assertEquals(1, ((VibrateCmd) msgs.get(0)).speeds.get(1).index);
        Assert.assertEquals(1, ((VibrateCmd) msgs.get(0)).speeds.get(1).getSpeed(), 0);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 1);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 1);
        Assert.assertEquals(uglyStr, jsonOut);
    }

}
