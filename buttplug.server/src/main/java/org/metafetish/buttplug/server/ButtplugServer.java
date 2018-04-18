package org.metafetish.buttplug.server;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.common.util.concurrent.SettableFuture;

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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass().getSimpleName());

    @NonNull
    private DeviceManager deviceManager;

    private Handler pingTimeoutHandler;
    private Runnable pingTimeoutCallback;

    private String serverName;
    private long maxPingTime;
    private boolean pingTimedOut;
    private boolean receivedRequestServerInfo;
    private long clientMessageVersion;

    public ButtplugServer(String serverName) {
        this(serverName, 0, null);
    }

    public ButtplugServer(String serverName, long maxPingTime) {
        this(serverName, maxPingTime, null);
    }

    public ButtplugServer(String serverName, DeviceManager deviceManager) {
        this(serverName, 0, deviceManager);
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

    public Future<ButtplugMessage> sendMessage(final ButtplugMessage msg) throws
            ExecutionException, InterruptedException, InvocationTargetException,
            IllegalAccessException {
        final SettableFuture<ButtplugMessage> promise = SettableFuture.create();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                ButtplugServer.this.bpLogger.trace(String.format("Got Message %s of type %s to send",
                        msg.id, msg.getClass().getSimpleName()));
                long id = msg.id;
                if (id == 0) {
                    promise.set(ButtplugServer.this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_MSG,
                            "Message Id 0 is reserved for outgoing system messages. Please use another Id."));
                } else if (msg instanceof IButtplugMessageOutgoingOnly) {
                    promise.set(ButtplugServer.this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_MSG, String.format(
                            "Message of type %s cannot be sent to server", msg.getClass().getSimpleName())));
                } else if (ButtplugServer.this.pingTimedOut) {
                    promise.set(ButtplugServer.this.bpLogger.logWarnMsg(id, Error.ErrorClass.ERROR_PING, "Ping timed out."));
                } else if (!ButtplugServer.this.receivedRequestServerInfo && !(msg instanceof RequestServerInfo)) {
                    promise.set(ButtplugServer.this.bpLogger.logErrorMsg(id, Error.ErrorClass.ERROR_INIT,
                            "RequestServerInfo must be first message received by server!"));
                } else  if (msg instanceof RequestLog) {
                    ButtplugServer.this.bpLogger.debug("Got RequestLog Message");
                    ButtplugServer.this.bpLogManager.setButtplugLogLevel(((RequestLog) msg).logLevel);
                    promise.set(new Ok(id));
                } else if (msg instanceof Ping) {
                    if (ButtplugServer.this.pingTimeoutHandler != null && ButtplugServer.this.maxPingTime != 0) {
                        ButtplugServer.this.pingTimeoutHandler.removeCallbacks(ButtplugServer.this.pingTimeoutCallback);
                        ButtplugServer.this.pingTimeoutHandler.postDelayed(ButtplugServer.this.pingTimeoutCallback, MAX_PING_TIMEOUT);
                    }
                    promise.set(new Ok(id));
                } else if (msg instanceof RequestServerInfo) {
                    ButtplugServer.this.bpLogger.debug("Got RequestServerInfo Message");
                    ButtplugServer.this.receivedRequestServerInfo = true;
                    ButtplugServer.this.clientMessageVersion = ((RequestServerInfo) msg).messageVersion;

                    if (ButtplugServer.this.pingTimeoutHandler != null && ButtplugServer.this.maxPingTime != 0) {
                        ButtplugServer.this.pingTimeoutHandler.postDelayed(ButtplugServer.this.pingTimeoutCallback, MAX_PING_TIMEOUT);
                    }
                    ButtplugServer.this.clientConnected.invoke(new ButtplugEvent(msg));

                    promise.set(new ServerInfo(ButtplugServer.this.serverName, 1, ButtplugServer.this.maxPingTime, id));
                    ButtplugServer.this.bpLogger.debug("RSI promise set");
                } else if (msg instanceof Test) {
                    promise.set(new Test(((Test) msg).getTestString(), id));
                } else {
                    try {
                        promise.set(ButtplugServer.this.deviceManager.sendMessage(msg).get());
                    } catch (InterruptedException | ExecutionException | InvocationTargetException | IllegalAccessException e) {
                        promise.set(new Error(e.getMessage(), Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.DefaultMsgId));

                    }
                }
            }
        });
        return promise;
    }

    public Future<Void> shutdown() throws ExecutionException, InterruptedException,
            InvocationTargetException, IllegalAccessException {
        final SettableFuture<Void> promise = SettableFuture.create();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                ButtplugMessage msg = null;
                try {
                    msg = ButtplugServer.this.deviceManager.sendMessage(new StopAllDevices()).get();
                } catch (InterruptedException | ExecutionException | IllegalAccessException | InvocationTargetException e) {
                    ButtplugServer.this.bpLogger.logException(e);
                }
                if (msg instanceof Error) {
                    ButtplugServer.this.bpLogger.error("An error occured while stopping devices on shutdown.");
                    ButtplugServer.this.bpLogger.error(((Error) msg).errorMessage);
                }

                ButtplugServer.this.deviceManager.stopScanning();
                ButtplugServer.this.deviceManager.getDeviceMessageReceived().removeCallback(ButtplugServer.this.deviceMessageReceivedCallback);
                ButtplugServer.this.deviceManager.getScanningFinished().removeCallback(ButtplugServer.this.scanningFinishedCallback);
                ButtplugServer.this.bpLogManager.getLogMessageReceived().removeCallback(ButtplugServer.this.logMessageReceivedCallback);
                promise.set(null);
            }
        });
        return promise;
    }

    public Future<List<ButtplugMessage>> sendMessage(final String jsonMsgs) throws
            ExecutionException, InterruptedException, IOException, InvocationTargetException,
            IllegalAccessException {
        final SettableFuture<List<ButtplugMessage>> promise = SettableFuture.create();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                List<ButtplugMessage> msgs = null;
                try {
                    msgs = ButtplugServer.this.parser.deserialize(jsonMsgs);
                    List<ButtplugMessage> res = new ArrayList<>();
                    for (ButtplugMessage msg : msgs) {
                        if (msg instanceof Error) {
                            res.add(msg);
                        } else {
                            try {
                                res.add(ButtplugServer.this.sendMessage(msg).get());
                            } catch (final InterruptedException | ExecutionException | InvocationTargetException | IllegalAccessException e) {
                                promise.set(new ArrayList<ButtplugMessage>(){{
                                    add(new Error(e.getMessage(), Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.DefaultMsgId));
                                }});
                                return;
                            }
                        }
                    }
                    promise.set(res);
                } catch (final IOException e) {
                    promise.set(new ArrayList<ButtplugMessage>(){{
                        add(new Error(e.getMessage(), Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.DefaultMsgId));
                    }});
                }
            }
        });
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
