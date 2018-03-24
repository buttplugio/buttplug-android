package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class RotateCmdTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"RotateCmd\": {\n" +
                "      \"Id\": 1,\n" +
                "      \"DeviceIndex\": 0,\n" +
                "      \"Rotations\": [\n" +
                "        {\n" +
                "          \"Index\": 0,\n" +
                "          \"Speed\": 0.5,\n" +
                "          \"Clockwise\": true\n" +
                "        },\n" +
                "        {\n" +
                "          \"Index\": 1,\n" +
                "          \"Speed\": 1.0,\n" +
                "          \"Clockwise\": false\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(RotateCmd.class, msgs.get(0).getClass());
        Assert.assertEquals(1, msgs.get(0).id);
        Assert.assertEquals(0, ((RotateCmd) msgs.get(0)).deviceIndex);
        Assert.assertEquals(2, ((RotateCmd) msgs.get(0)).rotations.size());
        Assert.assertEquals(0, ((RotateCmd) msgs.get(0)).rotations.get(0).index);
        Assert.assertEquals(0.5, ((RotateCmd) msgs.get(0)).rotations.get(0).getSpeed(), 0);
        Assert.assertEquals(true, ((RotateCmd) msgs.get(0)).rotations.get(0).clockwise);
        Assert.assertEquals(1, ((RotateCmd) msgs.get(0)).rotations.get(1).index);
        Assert.assertEquals(1.0, ((RotateCmd) msgs.get(0)).rotations.get(1).getSpeed(), 0);
        Assert.assertEquals(false, ((RotateCmd) msgs.get(0)).rotations.get(1).clockwise);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 1);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 1);
        Assert.assertEquals(uglyStr, jsonOut);
    }

}
