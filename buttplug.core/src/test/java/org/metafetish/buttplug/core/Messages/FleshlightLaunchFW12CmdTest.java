package org.metafetish.buttplug.core.Messages;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class FleshlightLaunchFW12CmdTest {

    @Test
    public void test() throws IOException {
        String testStr = "[{\"FleshlightLaunchFW12Cmd\":{\"Id\":7,\"DeviceIndex\":3,\"Speed\":15,\"Position\":85}}]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.deserialize(testStr);

        Assert.assertEquals(msgs.size(), 1);
        Assert.assertEquals(msgs.get(0).getClass(), FleshlightLaunchFW12Cmd.class);
        Assert.assertEquals(msgs.get(0).id, 7);
        Assert.assertEquals(((FleshlightLaunchFW12Cmd) msgs.get(0)).deviceIndex, 3);
        Assert.assertEquals(((FleshlightLaunchFW12Cmd) msgs.get(0)).getSpeed(), 15);
        Assert.assertEquals(((FleshlightLaunchFW12Cmd) msgs.get(0)).getPosition(), 85);

        String jsonOut = parser.serialize(msgs, 0);
        Assert.assertEquals(testStr, jsonOut);

        jsonOut = parser.serialize(msgs.get(0), 0);
        Assert.assertEquals(testStr, jsonOut);
    }

}
