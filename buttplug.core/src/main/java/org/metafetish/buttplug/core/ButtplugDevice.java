package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;

import com.google.common.util.concurrent.SettableFuture;

import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.metafetish.buttplug.core.Messages.Ok;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ButtplugDevice implements IButtplugDevice {
    protected String name;

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
    protected IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass().getSimpleName());

    @NonNull
    protected Map<String, ButtplugDeviceWrapper> msgFuncs;

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
        this.msgFuncs = new HashMap<>();
        this.name = name;
        this.identifier = identifier;
    }

    @NonNull
    public Iterable<String> getAllowedMessageTypes() {
        return this.msgFuncs.keySet();
    }

    @NonNull
    public MessageAttributes getMessageAttrs(String msgType) {
        if (this.msgFuncs.containsKey(msgType)) {
            return this.msgFuncs.get(msgType).attrs;
        }
        return new MessageAttributes();
    }

    protected void invokeDeviceRemoved() {
        this.isDisconnected = true;
        if (this.deviceRemoved != null) {
            this.deviceRemoved.invoke(new ButtplugEvent(this));
        }
    }

    @NonNull
    public Future<ButtplugMessage> parseMessage(final @NonNull ButtplugDeviceMessage msg) {
        final SettableFuture<ButtplugMessage> promise = SettableFuture.create();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                if (ButtplugDevice.this.isDisconnected) {
                    promise.set(new Error(String.format("%s has disconnected and can no longer process messages.",
                                    ButtplugDevice.this.name), Error.ErrorClass.ERROR_DEVICE, msg.id));
                } else if (!ButtplugDevice.this.msgFuncs.containsKey(msg.getClass().getSimpleName())) {
                    promise.set(new Error(String.format("%s cannot handle message of type %s",
                            ButtplugDevice.this.name, msg.getClass().getSimpleName()),
                            Error.ErrorClass.ERROR_DEVICE, msg.id));
                } else {
                    // We just checked whether the key exists above, so we're ok.
                    promise.set(ButtplugDevice.this.msgFuncs.get(msg.getClass().getSimpleName()).callback.invoke(msg));
                }
            }
        });
        return promise;
    }

    @NonNull
    public Future<ButtplugMessage> initialize() {
        final SettableFuture<ButtplugMessage> promise = SettableFuture.create();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                promise.set(new Ok(ButtplugConsts.SystemMsgId));
            }
        });
        return promise;
    }

    public abstract void disconnect();

    protected void emitMessage(ButtplugMessage msg) {
        if (this.messageEmitted != null) {
            this.messageEmitted.invoke(new ButtplugEvent(msg));
        }
    }
}
