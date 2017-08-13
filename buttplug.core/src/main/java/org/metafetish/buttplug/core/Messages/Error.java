package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Error extends ButtplugMessage {
    
    public enum ErrorClass
    {
        ERROR_UNKNOWN,
        ERROR_INIT,
        ERROR_PING,
        ERROR_MSG,
        ERROR_DEVICE,
    }

    @JsonProperty(value="ErrorCode", required = true)
    public ErrorClass errorCode;
    
    @JsonProperty(value = "ErrorMessage", required = true)
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Error(String errorMessage, ErrorClass errorCode) {
        super(ButtplugConsts.DefaultMsgId);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public Error(String errorMessage, ErrorClass errorCode, long id) {
        super(id);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
}
