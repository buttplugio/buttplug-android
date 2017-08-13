package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestServerInfo extends ButtplugMessage
{
    @JsonProperty(value="ClientName", required = true)
    public String clientName;

    public RequestServerInfo(String clientName, long id)
    {
        super(id);
        this.clientName = clientName;
    }

    public RequestServerInfo(String clientName)
    {
        super(ButtplugConsts.DefaultMsgId);
        this.clientName = clientName;
    }
    
    private RequestServerInfo()
    {
        super(ButtplugConsts.DefaultMsgId);
        this.clientName = "";
    }
}