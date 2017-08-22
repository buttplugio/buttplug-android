package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class RequestLog extends ButtplugMessage
{
    
    public enum ButtplugLogLevel
    {
        OFF("Off"),
        FATAL("Fatal"),
        ERROR("Error"),
        WARN("Warn"),
        INFO("Info"),
        DEBUG("Debug"),
        TRACE("Trace");

        public String jsonName;

        ButtplugLogLevel(String jsonName) {
            this.jsonName = jsonName;
        }

        @JsonValue
        @Override
        public String toString() {
            return jsonName;
        }
    }

    @JsonProperty(value="LogLevel", required = true)
    public ButtplugLogLevel logLevel;

    public RequestLog()
    {
        super(ButtplugConsts.DefaultMsgId);
        logLevel = ButtplugLogLevel.OFF;
    }
    
    public RequestLog(long id)
    { 
        super(id);
        logLevel = ButtplugLogLevel.OFF;
    }

    public RequestLog(ButtplugLogLevel logLevel, long id)
    { 
        super(id);
        this.logLevel = logLevel;
    }
    
    public RequestLog(ButtplugLogLevel logLevel)
    { 
        super(ButtplugConsts.DefaultMsgId);
        this.logLevel = logLevel;
    }
}
