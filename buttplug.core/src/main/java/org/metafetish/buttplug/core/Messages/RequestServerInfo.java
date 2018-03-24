package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id", "ClientName", "MessageVersion"})
public class RequestServerInfo extends ButtplugMessage {

    @JsonProperty(value = "ClientName", required = true)
    public String clientName;

    @JsonProperty(value = "MessageVersion")
    public long messageVersion;

    public RequestServerInfo(String clientName, long id, long messageVersion) {
        super(id);
        this.clientName = clientName;
        this.messageVersion = messageVersion;
    }

    @SuppressWarnings("unused")
    private RequestServerInfo() {
        super(ButtplugConsts.DefaultMsgId);
        this.clientName = "";
        this.messageVersion = currentSchemaVersion;
    }
}