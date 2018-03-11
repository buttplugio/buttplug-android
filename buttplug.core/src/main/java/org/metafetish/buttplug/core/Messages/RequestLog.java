package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugLogLevel;
import org.metafetish.buttplug.core.ButtplugMessage;

public class RequestLog extends ButtplugMessage {

    @JsonProperty(value = "LogLevel", required = true)
    public ButtplugLogLevel logLevel;

    @SuppressWarnings("unused")
    private RequestLog() {
        super(ButtplugConsts.DefaultMsgId);
        logLevel = ButtplugLogLevel.OFF;
    }

    public RequestLog(ButtplugLogLevel logLevel, long id) {
        super(id);
        this.logLevel = logLevel;
    }
}
