package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class ServerInfoTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"ServerInfo\": {\n" +
                "      \"Id\": 1,\n" +
                "      \"ServerName\": \"Test Server\",\n" +
                "      \"MajorVersion\": 1,\n" +
                "      \"MinorVersion\": 0,\n" +
                "      \"BuildVersion\": 0,\n" +
                "      \"MessageVersion\": 1,\n" +
                "      \"MaxPingTime\": 100\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(ServerInfo.class, msgs.get(0).getClass());
        Assert.assertEquals(1, msgs.get(0).id);
        Assert.assertEquals(1, ((ServerInfo) msgs.get(0)).majorVersion);
        Assert.assertEquals(0, ((ServerInfo) msgs.get(0)).minorVersion);
        Assert.assertEquals(0, ((ServerInfo) msgs.get(0)).buildVersion);
        Assert.assertEquals(1, ((ServerInfo) msgs.get(0)).messageVersion);
        Assert.assertEquals(100, ((ServerInfo) msgs.get(0)).maxPingTime);
        Assert.assertEquals("Test Server", ((ServerInfo) msgs.get(0)).serverName);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 0);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 0);
        Assert.assertEquals(uglyStr, jsonOut);
    }
}