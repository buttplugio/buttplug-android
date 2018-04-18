package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.Messages.MessageAttributes;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

public interface IButtplugDevice {
    @NonNull
    String getName();

    @NonNull
    String getIdentifier();

    @NonNull
    Long getIndex();

    void setIndex(long index);

    @NonNull
    Boolean isConnected();

    @NonNull
    ButtplugEventHandler getDeviceRemoved();

    @NonNull
    ButtplugEventHandler getMessageEmitted();

    @NonNull
    Iterable<String> getAllowedMessageTypes();

    @NonNull
    Future<ButtplugMessage> parseMessage(ButtplugDeviceMessage msg) throws
            InvocationTargetException, IllegalAccessException;

    @NonNull
    Future<ButtplugMessage> initialize();

    void disconnect();

    @NonNull
    MessageAttributes getMessageAttrs(String msgType);
}
