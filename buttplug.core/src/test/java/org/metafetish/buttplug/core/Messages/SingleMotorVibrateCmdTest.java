package org.metafetish.buttplug.core.Messages;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.io.IOException;
import java.util.List;

public class SingleMotorVibrateCmdTest {

    @Test
    public void test() throws IOException {
        String testStr = "[{\"SingleMotorVibrateCmd\":{\"Id\":7,\"DeviceIndex\":3,\"Speed\":0.6}}]";

        ButtplugJsonMessageParser parser = new ButtplugJsonMessageParser();
        List<ButtplugMessage> msgs = parser.parseJson(testStr);

        Assert.assertEquals(msgs.size(), 1);
        Assert.assertEquals(msgs.get(0).getClass(), SingleMotorVibrateCmd.class);
        Assert.assertEquals(msgs.get(0).id, 7);
        Assert.assertEquals(((SingleMotorVibrateCmd) msgs.get(0)).deviceIndex, 3);
        Assert.assertEquals(((SingleMotorVibrateCmd) msgs.get(0)).getSpeed(), 0.6, 0);

        String jsonOut = parser.formatJson(msgs);
        Assert.assertEquals(testStr, jsonOut);

        jsonOut = parser.formatJson(msgs.get(0));
        Assert.assertEquals(testStr, jsonOut);
    }

}
