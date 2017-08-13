package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Test extends ButtplugMessage {

    @JsonProperty(value = "TestString", required = true)
    private String testString;

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        if (testString == "Error")
        {
            throw new IllegalArgumentException("Got an Error Message");
        }
        this.testString = testString;
    }

    public Test(String testString) {
        super(ButtplugConsts.DefaultMsgId);
        this.testString = testString;
    }

    public Test(String testString, long id) {
        super(id);
        this.testString = testString;
    }
    
    private Test() {
        super(ButtplugConsts.DefaultMsgId);
        this.testString = "";
    }
}
