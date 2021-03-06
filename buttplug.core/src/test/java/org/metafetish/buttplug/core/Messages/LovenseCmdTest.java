package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class LovenseCmdTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"LovenseCmd\": {\n" +
                "      \"Id\": 1,\n" +
                "      \"DeviceIndex\": 0,\n" +
                "      \"Command\": \"Vibrate:20;\"\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(LovenseCmd.class, msgs.get(0).getClass());
        Assert.assertEquals(1, msgs.get(0).id);
        Assert.assertEquals(0, ((LovenseCmd) msgs.get(0)).deviceIndex);
        Assert.assertEquals("Vibrate:20;", ((LovenseCmd) msgs.get(0)).deviceCmd);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 0);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 0);
        Assert.assertEquals(uglyStr, jsonOut);
    }

}
