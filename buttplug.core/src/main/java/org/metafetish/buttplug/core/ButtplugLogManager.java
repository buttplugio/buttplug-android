package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.Messages.Log;

public class ButtplugLogManager implements IButtplugLogManager {
    private ButtplugEventHandler logMessageReceived = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getLogMessageReceived() {
        return this.logMessageReceived;
    }

    private ButtplugLogLevel level;

    public ButtplugLogLevel getButtplugLogLevel() {
        if (this.level != null) {
            return this.level;
        } else {
            return ButtplugLogLevel.TRACE;
        }
    }

    public void setButtplugLogLevel(ButtplugLogLevel level) {
        this.level = level;
    }

    private IButtplugCallback logMessageCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            Log msg = (Log) event.getMessage();
            if (msg.logLevel.ordinal() <= ButtplugLogManager.this.getButtplugLogLevel().ordinal
                    ()) {
                if (logMessageReceived != null) {
                    logMessageReceived.invoke(new ButtplugEvent(msg));
                }
            }
        }
    };

    @NonNull
    public IButtplugLog getLogger(@NonNull Class classDefinition) {
        // Just pass the type in instead of traversing the stack to find it.
        IButtplugLog logger = new ButtplugLog(classDefinition);
        logger.getLogMessageReceived().addCallback(logMessageCallback);
        return logger;
    }
}
