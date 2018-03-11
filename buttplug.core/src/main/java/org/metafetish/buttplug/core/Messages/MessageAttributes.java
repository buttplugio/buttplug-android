package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class MessageAttributes {
    @JsonInclude(Include.NON_NULL)
    public long featureCount;

    public MessageAttributes() {
    }

    public MessageAttributes(long featureCount) {
        this.featureCount = featureCount;
    }
}
