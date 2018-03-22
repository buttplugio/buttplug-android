package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugLogLevel;
import org.metafetish.buttplug.core.ButtplugMessage;

public class Log extends ButtplugMessage implements IButtplugMessageOutgoingOnly {
    @JsonProperty(value = "LogLevel", required = true)
    public ButtplugLogLevel logLevel;

    @JsonProperty(value = "LogMessage", required = true)
    public String logMessage;

    @JsonIgnore
    public String tag;

    public Log(ButtplugLogLevel logLevel, String logMessage, long id, String tag) {
        super(id);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.tag = tag;
    }

    public Log(ButtplugLogLevel logLevel, String logMessage, long id) {
        super(id);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.tag = null;
    }

    public Log(ButtplugLogLevel logLevel, String logMessage, String tag) {
        super(ButtplugConsts.DefaultMsgId);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.tag = tag;
    }

    public Log(ButtplugLogLevel logLevel, String logMessage) {
        super(ButtplugConsts.DefaultMsgId);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.tag = null;
    }

    @SuppressWarnings("unused")
    Log() {
        super(ButtplugConsts.DefaultMsgId);
        this.logLevel = ButtplugLogLevel.OFF;
        this.logMessage = "";
    }
}
