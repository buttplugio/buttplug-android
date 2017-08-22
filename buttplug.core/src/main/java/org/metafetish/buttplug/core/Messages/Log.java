package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.Messages.RequestLog.ButtplugLogLevel;

public class Log extends ButtplugMessage {
    @JsonProperty(value = "LogLevel", required = true)
    public ButtplugLogLevel logLevel;

    @JsonProperty(value = "LogMessage", required = true)
    public String logMessage;

    public Log(ButtplugLogLevel logLevel, String logMessage, long id) {
        super(id);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
    }

    @SuppressWarnings("unused")
    private Log() {
        super(ButtplugConsts.DefaultMsgId);
        this.logLevel = ButtplugLogLevel.OFF;
        this.logMessage = "";
    }
}
