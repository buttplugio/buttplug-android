package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.metafetish.buttplug.core.Messages.Log;

public class ButtplugLogManager implements IButtplugLogManager {
    @Nullable
    public ButtplugEventHandler logMessageReceived = new ButtplugEventHandler();
    private ButtplugLogLevel level;

    public ButtplugLogLevel getButtplugLogLevel() {
        if (this.level != null) {
            return this.level;
        } else {
            return ButtplugLogLevel.TRACE;
        }
    }

    public void setButtplugLogLevel(ButtplugLogLevel aLevel) {
        this.level = aLevel;
    }

    private IButtplugCallback logMessageCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent aEvent) {
            Log aMsg = (Log) aEvent.getMessage();
            if (aMsg.logLevel.ordinal() <= ButtplugLogManager.this.getButtplugLogLevel().ordinal()) {
                if (logMessageReceived != null) {
                    logMessageReceived.invoke(new ButtplugEvent(aMsg));
                }
            }
        }
    };

    @NonNull
    public IButtplugLog getLogger(@NonNull Class aClass) {
        // Just pass the type in instead of traversing the stack to find it.
        IButtplugLog logger = new ButtplugLog(aClass);
        logger.logMessageReceived.addCallback(logMessageCallback);
        return logger;
    }
}
