package org.metafetish.buttplug.client;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.SingleMotorVibrateCmd;
import org.metafetish.buttplug.core.Messages.StopAllDevices;

import java.net.URI;

public class ButtplugWSClientTest {

    @Ignore
    @Test
    public void TestConnect() throws Exception {
        ButtplugWSClient client = new ButtplugWSClient("Java Test");
        client.connect(new URI("wss://localhost:12345/buttplug"), true);
        Assert.assertTrue(client.startScanning());

        Thread.sleep(5000);
        client.requestDeviceList();
        for (DeviceMessageInfo deviceInfo : client.getDevices().values()) {
            if (deviceInfo.deviceMessages.keySet().contains(SingleMotorVibrateCmd.class.getSimpleName())) {
                ButtplugMessage msg = client.sendDeviceMessage(deviceInfo.deviceIndex, new SingleMotorVibrateCmd(deviceInfo.deviceIndex, 0.5, client
                        .getNextMsgId())).get();
                Assert.assertEquals(Ok.class, msg.getClass());
            }
        }

        Thread.sleep(1000);
        Assert.assertTrue(client.sendMessageExpectOk(new StopAllDevices(client.getNextMsgId())));

        client.disconnect();
    }
}