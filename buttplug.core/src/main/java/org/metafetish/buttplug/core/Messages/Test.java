package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id", "TestString"})
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
