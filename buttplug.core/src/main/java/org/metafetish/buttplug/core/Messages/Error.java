package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

@JsonPropertyOrder({"Id", "ErrorMessage", "ErrorCode"})
public class Error extends ButtplugMessage implements IButtplugMessageOutgoingOnly {

    public enum ErrorClass {
        ERROR_UNKNOWN,
        ERROR_INIT,
        ERROR_PING,
        ERROR_MSG,
        ERROR_DEVICE,
    }

    @JsonProperty(value = "ErrorMessage", required = true)
    public String errorMessage;

    @JsonProperty(value = "ErrorCode", required = true)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public ErrorClass errorCode;

    public Error(String errorMessage, ErrorClass errorCode, long id) {
        super(id);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    @SuppressWarnings("unused")
    private Error() {
        super(ButtplugConsts.DefaultMsgId);
        this.errorMessage = "";
        this.errorCode = ErrorClass.ERROR_UNKNOWN;
    }

}
