package org.metafetish.buttplug.server;

import android.support.annotation.NonNull;

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
import java.util.Timer;
import java.util.concurrent.ExecutionException;

public class ButtplugServer {
    @NonNull
    private ButtplugJsonMessageParser parser;

    private ButtplugEventHandler messageReceived = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getMessageReceived() {
        return this.messageReceived;
    }

    private ButtplugEventHandler clientConnected = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getClientConnected() {
        return this.clientConnected;
    }

    @NonNull
    protected IButtplugLogManager bpLogManager;

    @NonNull
    private IButtplugLog bpLogger;

    @NonNull
    private DeviceManager deviceManager;

    private Timer pingTimer;

    private String serverName;
    private long maxPingTime;
    private boolean pingTimedOut;
    private boolean receivedRequestServerInfo;
    @SuppressWarnings("FieldCanBeLocal")
    private long clientMessageVersion;

    public ButtplugServer(String serverName, long maxPingTime) {
        this(serverName, maxPingTime, null);
    }

    public ButtplugServer(@NonNull String serverName, long maxPingTime, DeviceManager
            deviceManager) {
        this.serverName = serverName;
        this.maxPingTime = maxPingTime;
        this.pingTimedOut = false;
        //TODO: Implement pingTimer

        this.bpLogManager = new ButtplugLogManager();
        this.bpLogger = this.bpLogManager.getLogger(this.getClass());
        this.bpLogger.debug("Setting up ButtplugServer");
        this.parser = new ButtplugJsonMessageParser();
        this.deviceManager = deviceManager != null ? deviceManager : new DeviceManager(this
                .bpLogManager);

        this.bpLogger.info("Finished setting up ButtplugServer");
        this.deviceManager.getDeviceMessageReceived().addCallback(this
                .deviceMessageReceivedCallback);
        this.deviceManager.getScanningFinished().addCallback(this.scanningFinishedCallback);
        this.bpLogManager.getLogMessageReceived().addCallback(this.logMessageReceivedCallback);
    }

    private IButtplugCallback deviceMessageReceivedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            if (ButtplugServer.this.messageReceived != null) {
                ButtplugServer.this.messageReceived.invoke(event);
            }
        }
    };

    private IButtplugCallback logMessageReceivedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            if (ButtplugServer.this.messageReceived != null) {
                ButtplugServer.this.messageReceived.invoke(event);
            }
        }
    };

    private IButtplugCallback scanningFinishedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            if (ButtplugServer.this.messageReceived != null) {
                ButtplugServer.this.messageReceived.invoke(new ButtplugEvent(new ScanningFinished
                        ()));
            }
        }
    };

    //TODO: Implement pingTimer

    protected ListenableFuture<ButtplugMessage> sendMessage(ButtplugMessage msg) throws
            ExecutionException, InterruptedException, InvocationTargetException,
            IllegalAccessException {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();

        this.bpLogger.trace("Got Message " + msg.id + " of type " + msg.getClass()
                .getSimpleName() + " to send");
        long id = msg.id;
        if (id == 0) {
            promise.set(this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_MSG, "Message Id 0 is" +
                    " reserved for outgoing system messages. Please use " + "another Id."));
            return promise;
        }

        if (msg instanceof IButtplugMessageOutgoingOnly) {
            promise.set(this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_MSG, "Message of type" +
                    " " + msg.getClass().getSimpleName() + " cannot be sent to server"));
            return promise;
        }


        if (this.pingTimedOut) {
            promise.set(this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_PING, "Ping timed out" +
                    "."));
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
            //TODO: Implement pingTimer
            promise.set(new Ok(id));
            return promise;
        } else if (msg instanceof RequestServerInfo) {
            this.bpLogger.debug("Got RequestServerInfo Message");
            this.receivedRequestServerInfo = true;
            this.clientMessageVersion = ((RequestServerInfo) msg).messageVersion;

            //TODO: Implement pingTimer
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
        List<ButtplugMessage> msgs = this.parser.parseJson(jsonMsgs);
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
        return this.parser.formatJson(msg);
    }

    public String serialize(List<ButtplugMessage> msgs) throws IOException {
        return this.parser.formatJson(msgs);
    }

    public List<ButtplugMessage> deserialize(String msg) throws IOException {
        return this.parser.parseJson(msg);
    }

    public void addDeviceSubtypeManager(IDeviceSubtypeManager mgr) {
        this.deviceManager.addDeviceSubtypeManager(mgr);
    }

    @NonNull
    public DeviceManager getDeviceManager() {
        return this.deviceManager;
    }
}
