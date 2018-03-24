package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id", "ServerName", "MajorVersion", "MinorVersion", "BuildVersion", "MessageVersion", "MaxPingTime"})
public class ServerInfo extends ButtplugMessage implements IButtplugMessageOutgoingOnly {

    @JsonProperty(value = "ServerName", required = true)
    public String serverName;

    //TODO: Be more consistent with use of long or int to replace uint
    @JsonProperty(value = "MajorVersion", required = true)
    public int majorVersion;

    @JsonProperty(value = "MinorVersion", required = true)
    public int minorVersion;

    @JsonProperty(value = "BuildVersion", required = true)
    public int buildVersion;

    @JsonProperty(value = "MessageVersion", required = true)
    public int messageVersion;

    @JsonProperty(value = "MaxPingTime", required = true)
    public long maxPingTime;

    public ServerInfo(String serverName, int messageVersion, long maxPingTime, long id) {
        super(id);

        this.serverName = serverName;
        this.messageVersion = messageVersion;
        this.maxPingTime = maxPingTime;
        this.majorVersion = 0;
        this.minorVersion = 0;
        this.buildVersion = 1;
    }

    @SuppressWarnings("unused")
    private ServerInfo() {
        super(ButtplugConsts.DefaultMsgId);

        this.serverName = "";
        this.messageVersion = 1;
        this.maxPingTime = 0;
        this.majorVersion = 0;
        this.minorVersion = 0;
        this.buildVersion = 1;
    }
}