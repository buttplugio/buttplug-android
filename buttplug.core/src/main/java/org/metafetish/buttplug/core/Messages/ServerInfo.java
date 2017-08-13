package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerInfo extends ButtplugMessage
{
    @JsonProperty(value="MajorVersion", required = true)
    public int majorVersion;

    @JsonProperty(value="MinorVersion", required = true)
    public int minorVersion;

    @JsonProperty(value="BuildVersion", required = true)
    public int buildVersion;

    @JsonProperty(value="MessageVersion", required = true)
    public int messageVersion;

    @JsonProperty(value="MaxPingTime", required = true)
    public long maxPingTime;

    @JsonProperty(value="ServerName", required = true)
    public String serverName;

    public ServerInfo(String serverName, int messageVersion, long maxPingTime, long id)
    { 
        super(id);
    
        this.serverName = serverName;
        this.messageVersion = messageVersion;
        this.maxPingTime = maxPingTime;
        this.majorVersion = 0;
        this.minorVersion = 0;
        this.buildVersion = 1;
    }
    
    public ServerInfo(String serverName, int messageVersion, long maxPingTime)
    { 
        super(ButtplugConsts.DefaultMsgId);
    
        this.serverName = serverName;
        this.messageVersion = messageVersion;
        this.maxPingTime = maxPingTime;
        this.majorVersion = 0;
        this.minorVersion = 0;
        this.buildVersion = 1;
    }
}