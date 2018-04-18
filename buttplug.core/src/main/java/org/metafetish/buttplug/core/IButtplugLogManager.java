package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

public interface IButtplugLogManager {
    @NonNull
    ButtplugEventHandler getLogMessageReceived();

    @NonNull
    IButtplugLog getLogger(String className);

    void setButtplugLogLevel(ButtplugLogLevel level);
}
