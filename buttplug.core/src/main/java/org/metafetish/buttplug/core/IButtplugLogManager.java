package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

public interface IButtplugLogManager {
    @NonNull
    ButtplugEventHandler getLogMessageReceived();

    @NonNull
    IButtplugLog getLogger(Class classDefinition);

    void setButtplugLogLevel(ButtplugLogLevel level);
}
