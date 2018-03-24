package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class FleshlightLaunchFW12CmdTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"FleshlightLaunchFW12Cmd\": {\n" +
                "      \"Id\": 1,\n" +
                "      \"DeviceIndex\": 0,\n" +
                "      \"Position\": 95,\n" +
                "      \"Speed\": 90\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(FleshlightLaunchFW12Cmd.class, msgs.get(0).getClass());
        Assert.assertEquals(1, msgs.get(0).id);
        Assert.assertEquals(0, ((FleshlightLaunchFW12Cmd) msgs.get(0)).deviceIndex);
        Assert.assertEquals(95, ((FleshlightLaunchFW12Cmd) msgs.get(0)).getPosition());
        Assert.assertEquals(90, ((FleshlightLaunchFW12Cmd) msgs.get(0)).getSpeed());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 0);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 0);
        Assert.assertEquals(uglyStr, jsonOut);
    }

}
