package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    void setIndex(long aIndex);

    @NonNull
    Boolean isConnected();

    @NonNull
    ButtplugEventHandler getDeviceRemoved();

    @NonNull
    ButtplugEventHandler getMessageEmitted();

    @NonNull
    Iterable<Class> getAllowedMessageTypes();

    @NonNull
    ListenableFuture<ButtplugMessage> parseMessage(ButtplugDeviceMessage aMsg) throws
            InvocationTargetException, IllegalAccessException;

    @NonNull
    ListenableFuture<ButtplugMessage> initialize();

    void disconnect();

    @NonNull
    MessageAttributes getMessageAttrs(Class aMsg);
}
