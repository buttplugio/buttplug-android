package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class LinearCmdTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"LinearCmd\": {\n" +
                "      \"Id\": 1,\n" +
                "      \"DeviceIndex\": 0,\n" +
                "      \"Vectors\": [\n" +
                "        {\n" +
                "          \"Index\": 0,\n" +
                "          \"Duration\": 500,\n" +
                "          \"Position\": 0.3\n" +
                "        },\n" +
                "        {\n" +
                "          \"Index\": 1,\n" +
                "          \"Duration\": 1000,\n" +
                "          \"Position\": 0.8\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(LinearCmd.class, msgs.get(0).getClass());
        Assert.assertEquals(1, msgs.get(0).id);
        Assert.assertEquals(0, ((LinearCmd) msgs.get(0)).deviceIndex);
        Assert.assertEquals(2, ((LinearCmd) msgs.get(0)).vectors.size());
        Assert.assertEquals(0, ((LinearCmd) msgs.get(0)).vectors.get(0).index);
        Assert.assertEquals(500, ((LinearCmd) msgs.get(0)).vectors.get(0).duration);
        Assert.assertEquals(0.3, ((LinearCmd) msgs.get(0)).vectors.get(0).getPosition(), 0);
        Assert.assertEquals(1, ((LinearCmd) msgs.get(0)).vectors.get(1).index);
        Assert.assertEquals(1000, ((LinearCmd) msgs.get(0)).vectors.get(1).duration);
        Assert.assertEquals(0.8, ((LinearCmd) msgs.get(0)).vectors.get(1).getPosition(), 0);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 1);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 1);
        Assert.assertEquals(uglyStr, jsonOut);
    }

}
