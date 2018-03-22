package org.metafetish.buttplug.server;

import android.os.Handler;
import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.IButtplugMessageOutgoingOnly;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.Ping;
import org.metafetish.buttplug.core.Messages.RequestLog;
import org.metafetish.buttplug.core.Messages.RequestServerInfo;
import org.metafetish.buttplug.core.Messages.ScanningFinished;
import org.metafetish.buttplug.core.Messages.ServerInfo;
import org.metafetish.buttplug.core.Messages.StopAllDevices;
import org.metafetish.buttplug.core.Messages.Test;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ButtplugServer {
    private static final int MAX_PING_TIMEOUT = 5000;

    @NonNull
    private ButtplugJsonMessageParser parser;

    @NonNull
    private ButtplugEventHandler messageReceived = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getMessageReceived() {
        return this.messageReceived;
    }

    @NonNull
    private ButtplugEventHandler clientConnected = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getClientConnected() {
        return this.clientConnected;
    }

    @NonNull
    protected IButtplugLogManager bpLogManager = new ButtplugLogManager();

    @NonNull
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

    @NonNull
    private DeviceManager deviceManager;

    private Handler pingTimeoutHandler;
    private Runnable pingTimeoutCallback;

    private String serverName;
    private long maxPingTime;
    private boolean pingTimedOut;
    private boolean receivedRequestServerInfo;
    private long clientMessageVersion;

    public ButtplugServer(String serverName, long maxPingTime) {
        this(serverName, maxPingTime, null);
    }

    public ButtplugServer(@NonNull String serverName, long maxPingTime, DeviceManager
            deviceManager) {
        this.serverName = serverName;
        this.maxPingTime = maxPingTime;
        this.pingTimedOut = false;
        if (maxPingTime != 0) {
            this.pingTimeoutCallback = new Runnable() {
                @Override
                public void run() {
                    ButtplugServer.this.handlePingTimeout();
                }
            };
        }

        this.bpLogger.debug("Setting up ButtplugServer");
        this.parser = new ButtplugJsonMessageParser();
        this.deviceManager = deviceManager != null ? deviceManager : new DeviceManager(this.bpLogManager);

        this.bpLogger.info("Finished setting up ButtplugServer");
        this.deviceManager.getDeviceMessageReceived().addCallback(this.deviceMessageReceivedCallback);
        this.deviceManager.getScanningFinished().addCallback(this.scanningFinishedCallback);
        this.bpLogManager.getLogMessageReceived().addCallback(this.logMessageReceivedCallback);
    }

    public void setHandler(Handler handler) {
        this.pingTimeoutHandler = handler;
    }

    private IButtplugCallback deviceMessageReceivedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ButtplugServer.this.messageReceived.invoke(event);
        }
    };

    private IButtplugCallback logMessageReceivedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ButtplugServer.this.messageReceived.invoke(event);
        }
    };

    private IButtplugCallback scanningFinishedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ButtplugServer.this.messageReceived.invoke(new ButtplugEvent(new ScanningFinished()));
        }
    };

    private void handlePingTimeout()
    {
        this.messageReceived.invoke(new ButtplugEvent(new Error("Ping timed out.",
                Error.ErrorClass.ERROR_PING, ButtplugConsts.SystemMsgId)));
        try {
            this.sendMessage(new StopAllDevices()).get();
        } catch (InterruptedException | ExecutionException | IllegalAccessException | InvocationTargetException e) {
            this.bpLogger.error("Failed to issue StopAllDevices.");
            this.bpLogger.logException(e);
        }
        this.pingTimedOut = true;
    }

    protected ListenableFuture<ButtplugMessage> sendMessage(ButtplugMessage msg) throws
            ExecutionException, InterruptedException, InvocationTargetException,
            IllegalAccessException {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();

        this.bpLogger.trace(String.format("Got Message %s of type %s to send",
                msg.id,
                msg.getClass().getSimpleName()));
        long id = msg.id;
        if (id == 0) {
            promise.set(this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_MSG,"Message Id 0" +
                    " is reserved for outgoing system messages. Please use another Id."));
            return promise;
        }

        if (msg instanceof IButtplugMessageOutgoingOnly) {
            promise.set(this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_MSG, String.format(
                    "Message of type %s cannot be sent to server",
                    msg.getClass().getSimpleName())));
            return promise;
        }


        if (this.pingTimedOut) {
            promise.set(this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_PING, "Ping timed out."));
            return promise;
        }

        if (!this.receivedRequestServerInfo && !(msg instanceof RequestServerInfo)) {
            promise.set(this.bpLogger.logErrorMsg(id, Error.ErrorClass.ERROR_INIT,
                    "RequestServerInfo must be first message received by server!"));
            return promise;
        }

        if (msg instanceof RequestLog) {
            this.bpLogger.debug("Got RequestLog Message");
            this.bpLogManager.setButtplugLogLevel(((RequestLog) msg).logLevel);
            promise.set(new Ok(id));
            return promise;
        } else if (msg instanceof Ping) {
            if (this.pingTimeoutHandler != null && this.maxPingTime != 0) {
                this.pingTimeoutHandler.removeCallbacks(this.pingTimeoutCallback);
                this.pingTimeoutHandler.postDelayed(this.pingTimeoutCallback, MAX_PING_TIMEOUT);
            }
            promise.set(new Ok(id));
            return promise;
        } else if (msg instanceof RequestServerInfo) {
            this.bpLogger.debug("Got RequestServerInfo Message");
            this.receivedRequestServerInfo = true;
            this.clientMessageVersion = ((RequestServerInfo) msg).messageVersion;

            if (this.pingTimeoutHandler != null && this.maxPingTime != 0) {
                this.pingTimeoutHandler.postDelayed(this.pingTimeoutCallback, MAX_PING_TIMEOUT);
            }
            this.clientConnected.invoke(new ButtplugEvent(msg));

            promise.set(new ServerInfo(this.serverName, 1, this.maxPingTime, id));
            return promise;
        } else if (msg instanceof Test) {
            promise.set(new Test(((Test) msg).getTestString(), id));
            return promise;
        }
        promise.set(this.deviceManager.sendMessage(msg).get());
        return promise;
    }

    public ListenableFuture<Void> shutdown() throws ExecutionException, InterruptedException,
            InvocationTargetException, IllegalAccessException {
        SettableListenableFuture<Void> promise = new SettableListenableFuture<>();
        ButtplugMessage msg = this.deviceManager.sendMessage(new StopAllDevices()).get();
        if (msg instanceof Error) {
            this.bpLogger.error("An error occured while stopping devices on shutdown.");
            this.bpLogger.error(((Error) msg).errorMessage);
        }

        this.deviceManager.stopScanning();
        this.deviceManager.getDeviceMessageReceived().removeCallback(this
                .deviceMessageReceivedCallback);
        this.deviceManager.getScanningFinished().removeCallback(this.scanningFinishedCallback);
        this.bpLogManager.getLogMessageReceived().removeCallback(this.logMessageReceivedCallback);
        return promise;
    }

    public ListenableFuture<List<ButtplugMessage>> sendMessage(String jsonMsgs) throws
            ExecutionException, InterruptedException, IOException, InvocationTargetException,
            IllegalAccessException {
        SettableListenableFuture<List<ButtplugMessage>> promise = new SettableListenableFuture<>();
        List<ButtplugMessage> msgs = this.parser.deserialize(jsonMsgs);
        List<ButtplugMessage> res = new ArrayList<>();
        for (ButtplugMessage msg : msgs) {
            if (msg instanceof Error) {
                res.add(msg);
            } else {
                res.add(this.sendMessage(msg).get());
            }
        }
        promise.set(res);
        return promise;
    }

    public String serialize(ButtplugMessage msg) throws IOException {
        return this.parser.serialize(msg, this.clientMessageVersion);
    }

    public String serialize(List<ButtplugMessage> msgs) throws IOException {
        return this.parser.serialize(msgs, this.clientMessageVersion);
    }

    public List<ButtplugMessage> deserialize(String msg) throws IOException {
        return this.parser.deserialize(msg);
    }

    public void addDeviceSubtypeManager(IDeviceSubtypeManager mgr) {
        this.deviceManager.addDeviceSubtypeManager(mgr);
    }

    @NonNull
    public DeviceManager getDeviceManager() {
        return this.deviceManager;
    }
}
