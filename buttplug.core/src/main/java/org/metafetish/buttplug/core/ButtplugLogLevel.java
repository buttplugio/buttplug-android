package org.metafetish.buttplug.core;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ButtplugLogLevel {
    OFF("Off"),
    FATAL("Fatal"),
    ERROR("Error"),
    WARN("Warn"),
    INFO("Info"),
    DEBUG("Debug"),
    TRACE("Trace");

    private String jsonName;

    ButtplugLogLevel(String jsonName) {
        this.jsonName = jsonName;
    }

    @JsonValue
    @Override
    public String toString() {
        return jsonName;
    }
}
