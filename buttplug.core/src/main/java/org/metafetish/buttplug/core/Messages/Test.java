package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class Test extends ButtplugMessage {

    @JsonProperty(value = "TestString", required = true)
    private String testString;

    public Test(String testString, long id) {
        super(id);
        this.testString = testString;
    }

    @SuppressWarnings("unused")
    private Test() {
        super(ButtplugConsts.DefaultMsgId);
        this.testString = "";
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        if (testString.contentEquals("Error")) {
            throw new IllegalArgumentException("Got an Error Message");
        }
        this.testString = testString;
    }
}
