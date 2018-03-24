package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"FeatureCount"})
public class MessageAttributes {

    @JsonProperty(value = "FeatureCount")
    @JsonInclude(Include.NON_DEFAULT)
    public long featureCount;

    public MessageAttributes() {
    }

    public MessageAttributes(long featureCount) {
        this.featureCount = featureCount;
    }
}
