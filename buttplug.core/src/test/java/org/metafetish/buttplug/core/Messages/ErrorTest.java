package org.metafetish.buttplug.core.Messages;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class ErrorTest {

    @Test
    public void test() throws IOException {
        String testStr = "[{\"Error\":{\"Id\":7,\"ErrorCode\":4,\"ErrorMessage\":\"TestError\"}}]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.parseJson(testStr);

        Assert.assertEquals(msgs.size(), 1);
        Assert.assertEquals(msgs.get(0).getClass(),
                org.metafetish.buttplug.core.Messages.Error.class);
        Assert.assertEquals(msgs.get(0).id, 7);
        Assert.assertEquals(
                ((org.metafetish.buttplug.core.Messages.Error) msgs.get(0)).getErrorMessage(),
                "TestError");
        Assert.assertEquals(
                ((Error) msgs.get(0)).errorCode,
                Error.ErrorClass.ERROR_DEVICE);

        String jsonOut = parser.formatJson(msgs);
        Assert.assertEquals(testStr, jsonOut);

        jsonOut = parser.formatJson(msgs.get(0));
        Assert.assertEquals(testStr, jsonOut);
    }

}
