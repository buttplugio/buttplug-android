package org.metafetish.buttplug.client;

import org.junit.Ignore;
import org.junit.Test;
import org.metafetish.buttplug.core.Messages.SingleMotorVibrateCmd;
import org.metafetish.buttplug.core.Messages.StopAllDevices;

import java.net.URI;

public class ButtplugWSClientTest {

    @Ignore
    @Test
    public void TestConnect() throws Exception {
        ButtplugWSClient client = new ButtplugWSClient("Java Test");
        client.Connect(new URI("ws://localhost:12345/buttplug"));
        client.StartScanning();

        Thread.sleep(5000);
        client.RequestDeviceList();
        for (ButtplugClientDevice dev : client.getDevices()) {
            if (dev.allowedMessages.contains(SingleMotorVibrateCmd.class.getSimpleName())) {
                client.SendDeviceMessage(dev, new SingleMotorVibrateCmd(dev.index, 0.5));
            }
        }

        Thread.sleep(1000);
        client.SendMessageExpectOk(new StopAllDevices());

        client.Disconnect();
    }
}