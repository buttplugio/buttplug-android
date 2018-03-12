package org.metafetish.buttplug.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    public void setIndex(long aIndex) {
        this.index = aIndex;
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
    public ButtplugEventHandler getMessageEmitted()  {
        return this.messageEmitted;
    }

    @NonNull
    protected IButtplugLog bpLogger;

    @NonNull
    protected Map<Class, ButtplugDeviceWrapper> msgFuncs;

    private boolean isDisconnected;

    public class ButtplugDeviceWrapper {
        public IButtplugMessageCallback callback;
        public MessageAttributes attrs;

        public ButtplugDeviceWrapper(IButtplugMessageCallback aCallback) {
            this(aCallback, new MessageAttributes());
        }

        public ButtplugDeviceWrapper(IButtplugMessageCallback aCallback, MessageAttributes aAttrs) {
            this.callback = aCallback;
            this.attrs = aAttrs;
        }
    }

    protected ButtplugDevice(@NonNull IButtplugLogManager aLogManager,
                             @NonNull String aName,
                             @NonNull String aIdentifier) {
        bpLogger = aLogManager.getLogger(this.getClass());
        msgFuncs = new HashMap<>();
        name = aName;
        identifier = aIdentifier;
    }

    @NonNull
    public Iterable<Class> getAllowedMessageTypes() {
        return this.msgFuncs.keySet();
    }

    public MessageAttributes getMessageAttrs(Class aMsg) {
        if (this.msgFuncs.containsKey(aMsg)) {
            return this.msgFuncs.get(aMsg).attrs;
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
    public ListenableFuture<ButtplugMessage> parseMessage(@NonNull ButtplugDeviceMessage aMsg)
            throws InvocationTargetException, IllegalAccessException {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();
        if (this.isDisconnected) {
            promise.set(
                    new Error(this.name + " has disconnected and can no longer process messages.",
                            Error.ErrorClass.ERROR_DEVICE, aMsg.id));
            return promise;
        }
        if (!msgFuncs.containsKey(aMsg.getClass())) {
            promise.set(
                    new Error(this.name + " cannot handle message of type " + aMsg.getClass()
                            .getSimpleName(), Error.ErrorClass.ERROR_DEVICE, aMsg.id));
            return promise;
        }
        // We just checked whether the key exists above, so we're ok.
        promise.set(this.msgFuncs.get(aMsg.getClass()).callback.invoke(aMsg));
        return promise;
    }

    @NonNull
    public ListenableFuture<ButtplugMessage> initialize() {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();
        promise.set(new Ok(ButtplugConsts.SystemMsgId));
        return promise;
    }

    public abstract void disconnect();

    protected void emitMessage(ButtplugMessage aMsg) {
        if (this.messageEmitted != null) {
            this.messageEmitted.invoke(new ButtplugEvent(aMsg));
        }
    }
}
