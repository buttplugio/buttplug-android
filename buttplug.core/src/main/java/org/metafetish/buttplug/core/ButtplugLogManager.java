package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.metafetish.buttplug.core.Messages.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ButtplugLogManager implements IButtplugLogManager {
    public static ButtplugEventHandler globalLogMessageReceived = new ButtplugEventHandler();
    public static List<Pair<Date, Log>> lastLogMessagesReceived = new LinkedList<>();

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
                logMessageReceived.invoke(event);
            }
            ButtplugLogManager.lastLogMessagesReceived.add(new Pair<Date, Log>(new Date(), msg));
            if (ButtplugLogManager.lastLogMessagesReceived.size() > 25) {
                ButtplugLogManager.lastLogMessagesReceived.remove(0);
            }
            ButtplugLogManager.globalLogMessageReceived.invoke(event);
        }
    };

    @NonNull
    public IButtplugLog getLogger(@NonNull String className) {
        // Just pass the type in instead of traversing the stack to find it.
        IButtplugLog logger = new ButtplugLog(className);
        logger.getLogMessageReceived().addCallback(logMessageCallback);
        return logger;
    }
}
