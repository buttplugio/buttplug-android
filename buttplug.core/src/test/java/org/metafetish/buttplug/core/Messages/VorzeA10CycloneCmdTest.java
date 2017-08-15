package org.metafetish.buttplug.core.Messages;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class VorzeA10CycloneCmdTest {

    @Test
    public void test() throws IOException {
        String testStr = "[{\"VorzeA10CycloneCmd\":{\"Id\":7,\"DeviceIndex\":3,\"Speed\":15,\"Clockwise\":true}}]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.parseJson(testStr);

        Assert.assertEquals(msgs.size(), 1);
        Assert.assertEquals(msgs.get(0).getClass(), VorzeA10CycloneCmd.class);
        Assert.assertEquals(msgs.get(0).id, 7);
        Assert.assertEquals(((VorzeA10CycloneCmd) msgs.get(0)).deviceIndex, 3);
        Assert.assertEquals(((VorzeA10CycloneCmd) msgs.get(0)).GetSpeed(), 15);
        Assert.assertEquals(((VorzeA10CycloneCmd) msgs.get(0)).clockwise, true);

        String jsonOut = parser.formatJson(msgs);
        Assert.assertEquals(testStr, jsonOut);

        jsonOut = parser.formatJson(msgs.get(0));
        Assert.assertEquals(testStr, jsonOut);
    }

}
