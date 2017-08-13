package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class RequestLog extends ButtplugMessage
{
    
    public enum ButtplugLogLevel
    {
        OFF,
        FATAL,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE;
        
        @JsonValue
        public int toValue() {
            return ordinal();
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
