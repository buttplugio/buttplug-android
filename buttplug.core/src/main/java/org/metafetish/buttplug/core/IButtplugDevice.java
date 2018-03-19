package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.InvocationTargetException;

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
    Iterable<Class> getAllowedMessageTypes();

    @NonNull
    ListenableFuture<ButtplugMessage> parseMessage(ButtplugDeviceMessage msg) throws
            InvocationTargetException, IllegalAccessException;

    @NonNull
    ListenableFuture<ButtplugMessage> initialize();

    void disconnect();

    @NonNull
    MessageAttributes getMessageAttrs(Class msg);
}
