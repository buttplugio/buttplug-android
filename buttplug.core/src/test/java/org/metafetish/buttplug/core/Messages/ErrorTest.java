package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.Messages.Error;

import java.io.IOException;
import java.util.List;

public class ErrorTest {

    @Test
    public void test() throws IOException {
        String testStr = "[\n" +
                "  {\n" +
                "    \"Error\": {\n" +
                "      \"Id\": 0,\n" +
                "      \"ErrorMessage\": \"Server received invalid JSON.\",\n" +
                "      \"ErrorCode\": 3\n" +
                "    }\n" +
                "  }\n" +
                "]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(Error.class, msgs.get(0).getClass());
        Assert.assertEquals(0, msgs.get(0).id);
        Assert.assertEquals("Server received invalid JSON.", ((Error) msgs.get(0)).errorMessage);
        Assert.assertEquals(Error.ErrorClass.ERROR_MSG, ((Error) msgs.get(0)).errorCode);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(testStr, JsonNode.class);
        String uglyStr = jsonNode.toString();

        String jsonOut = parser.serialize(msgs, 0);
        Assert.assertEquals(uglyStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 0);
        Assert.assertEquals(uglyStr, jsonOut);
    }

}
