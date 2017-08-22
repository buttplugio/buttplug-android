package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

public class RequestServerInfo extends ButtplugMessage {
    @JsonProperty(value = "ClientName", required = true)
    public String clientName;

    public RequestServerInfo(String clientName, long id) {
        super(id);
        this.clientName = clientName;
    }

    @SuppressWarnings("unused")
    private RequestServerInfo() {
        super(ButtplugConsts.DefaultMsgId);
        this.clientName = "";
    }
}