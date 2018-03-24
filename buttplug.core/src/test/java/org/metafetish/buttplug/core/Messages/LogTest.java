package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugLogLevel;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class LogTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"Log\": {\n" +
                "      \"Id\": 0,\n" +
                "      \"LogLevel\": \"Trace\",\n" +
                "      \"LogMessage\": \"This is a Log Message.\"\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(Log.class, msgs.get(0).getClass());
        Assert.assertEquals(0, msgs.get(0).id);
        Assert.assertEquals(ButtplugLogLevel.TRACE, ((Log) msgs.get(0)).logLevel);
        Assert.assertEquals("This is a Log Message.", ((Log) msgs.get(0)).logMessage);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 0);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 0);
        Assert.assertEquals(uglyStr, jsonOut);
    }

}
