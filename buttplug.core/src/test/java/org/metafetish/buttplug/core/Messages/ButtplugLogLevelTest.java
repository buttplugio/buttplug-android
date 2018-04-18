package org.metafetish.buttplug.core.Messages;

import org.junit.Assert;
import org.junit.Test;
import org.metafetish.buttplug.core.ButtplugLogLevel;

import java.io.IOException;

public class ButtplugLogLevelTest {

    @Test
    public void test() {
        for (ButtplugLogLevel lvl : ButtplugLogLevel.values()) {
            Assert.assertEquals(lvl.name().toLowerCase(), lvl.toString().toLowerCase());
            Assert.assertEquals(lvl.name(), lvl.toString().toUpperCase());
            Assert.assertNotEquals(lvl.name(), lvl.toString());
        }
    }

}
