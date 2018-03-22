package org.metafetish.buttplug.core.Messages;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class OkTest {

    @Test
    public void test() throws IOException {
        String testStr = "[{\"Ok\":{\"Id\":3}}]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals(Ok.class, msgs.get(0).getClass());
        Assert.assertEquals(3, msgs.get(0).id);

        String jsonOut = parser.serialize(msgs, 0);
        Assert.assertEquals(testStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 0);
        Assert.assertEquals(testStr, jsonOut);
    }

}
