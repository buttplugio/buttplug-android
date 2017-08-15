package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugMessage;

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
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
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

    private Error() {
        super(ButtplugConsts.DefaultMsgId);
        this.errorMessage = "";
        this.errorCode = ErrorClass.ERROR_UNKNOWN;
    }
}
