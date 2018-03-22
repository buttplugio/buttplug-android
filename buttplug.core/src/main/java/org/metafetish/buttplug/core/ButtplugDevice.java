package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.metafetish.buttplug.core.Messages.Ok;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class ButtplugDevice implements IButtplugDevice {
    private String name;

    @NonNull
    public String getName() {
        return this.name;
    }

    private String identifier;

    @NonNull
    public String getIdentifier() {
        return this.identifier;
    }

    private long index;

    @NonNull
    public Long getIndex() {
        return this.index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    @NonNull
    public Boolean isConnected() {
        return !this.isDisconnected;
    }

    private ButtplugEventHandler deviceRemoved = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getDeviceRemoved() {
        return this.deviceRemoved;
    }

    private ButtplugEventHandler messageEmitted = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getMessageEmitted() {
        return this.messageEmitted;
    }

    @NonNull
    protected IButtplugLogManager bpLogManager = new ButtplugLogManager();

    @NonNull
    protected IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

    @NonNull
    protected Map<Class, ButtplugDeviceWrapper> msgFuncs;

    private boolean isDisconnected;

    public class ButtplugDeviceWrapper {
        public IButtplugDeviceMessageCallback callback;
        public MessageAttributes attrs;

        public ButtplugDeviceWrapper(IButtplugDeviceMessageCallback callback) {
            this(callback, new MessageAttributes());
        }

        public ButtplugDeviceWrapper(IButtplugDeviceMessageCallback callback, MessageAttributes
                attrs) {
            this.callback = callback;
            this.attrs = attrs;
        }
    }

    protected ButtplugDevice(@NonNull String name,
                             @NonNull String identifier) {
        msgFuncs = new HashMap<>();
        this.name = name;
        this.identifier = identifier;
    }

    @NonNull
    public Iterable<Class> getAllowedMessageTypes() {
        return this.msgFuncs.keySet();
    }

    public MessageAttributes getMessageAttrs(Class msg) {
        if (this.msgFuncs.containsKey(msg)) {
            return this.msgFuncs.get(msg).attrs;
        }
        return new MessageAttributes();
    }

    protected void invokeDeviceRemoved() {
        this.isDisconnected = true;
        if (this.deviceRemoved != null) {
            this.deviceRemoved.invoke(new ButtplugEvent());
        }
    }

    @NonNull
    public ListenableFuture<ButtplugMessage> parseMessage(@NonNull ButtplugDeviceMessage msg)
            throws InvocationTargetException, IllegalAccessException {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();
        if (this.isDisconnected) {
            promise.set(new Error(
                    String.format("%s has disconnected and can no longer process messages.",
                            this.name),
                    Error.ErrorClass.ERROR_DEVICE, msg.id));
            return promise;
        }
        if (!msgFuncs.containsKey(msg.getClass())) {
            promise.set(new Error(
                    String.format("%s cannot handle message of type %s",
                        this.name,
                        msg.getClass().getSimpleName()),
                    Error.ErrorClass.ERROR_DEVICE, msg.id));
            return promise;
        }
        // We just checked whether the key exists above, so we're ok.
        promise.set(this.msgFuncs.get(msg.getClass()).callback.invoke(msg));
        return promise;
    }

    @NonNull
    public ListenableFuture<ButtplugMessage> initialize() {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();
        promise.set(new Ok(ButtplugConsts.SystemMsgId));
        return promise;
    }

    public abstract void disconnect();

    protected void emitMessage(ButtplugMessage msg) {
        if (this.messageEmitted != null) {
            this.messageEmitted.invoke(new ButtplugEvent(msg));
        }
    }
}
