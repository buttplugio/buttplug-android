package org.metafetish.buttplug.client;

import com.google.common.util.concurrent.SettableFuture;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.Ping;
import org.metafetish.buttplug.core.Messages.ServerInfo;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ButtplugWSClient extends ButtplugClient {
    private WebSocketClient ws;
    private Timer pingTimer;
    public ButtplugEventHandler connected = new ButtplugEventHandler();
    public ButtplugEventHandler disconnected = new ButtplugEventHandler();

    public ButtplugWSClient(String clientName) {
        super(clientName);
    }

    public void connect(URI url) throws Exception {
        connect(url, false);
    }

    public void connect(String url, boolean trustAll) throws Exception {
        this.connect(new URI(url), trustAll);
    }

    public void connect(URI url, boolean trustAll) throws Exception {
        super.connect();
        this.ws = getWebSocket(url, trustAll);
    }

    @Override
    public void onConnect() throws Exception {
        this.connected.invoke(new ButtplugEvent());
        super.onConnect();
    }

    @Override
    public void disconnect() {
        if (this.pingTimer != null) {
            this.pingTimer.cancel();
            this.pingTimer = null;
        }

        if (this.ws != null) {
            this.ws.close();
            this.ws = null;
        }
        super.disconnect();
        this.disconnected.invoke(new ButtplugEvent());
    }

    @Override
    public void onServerInfo(ServerInfo msg) throws Exception {
        if (msg.maxPingTime > 0) {
            this.pingTimer = new Timer("pingTimer", true);
            this.pingTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        onPingTimer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, Math.round((double) msg.maxPingTime / 2));
        }
        super.onServerInfo(msg);
    }

    private void onPingTimer() throws Exception {
        try {
            ButtplugMessage msg = sendMessage(new Ping(this.msgId.incrementAndGet())).get();
            if (msg instanceof Error) {
                throw new Exception(((Error) msg).errorMessage);
            }
        } catch (Throwable e) {
            this.disconnect();
            throw e;
        }
    }

    @Override
    protected Future<ButtplugMessage> sendMessage(final ButtplugMessage msg) {
        final SettableFuture<ButtplugMessage> promise = SettableFuture.create();

        this.waitingMsgs.put(msg.id, promise);
        if (this.ws == null) {
            promise.set(new Error("Bad WS state!", Error.ErrorClass.ERROR_UNKNOWN,
                    ButtplugConsts.SystemMsgId));
            return promise;
        }

        try {
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        ButtplugWSClient.this.ws.send(ButtplugWSClient.this.parser.serialize(msg,
                                    1));
                    } catch (IOException e) {
                        promise.set(new Error(e.getMessage(), Error.ErrorClass.ERROR_UNKNOWN, msg.id));
                    }
                }
            });
        } catch (WebsocketNotConnectedException e) {
            this.bpLogger.debug("exception sending message");
            promise.set(new Error(e.getMessage(), Error.ErrorClass.ERROR_UNKNOWN, msg.id));
        }

        return promise;
    }

    protected WebSocketClient getWebSocket(URI url, boolean trustAll)
            throws NoSuchAlgorithmException, KeyManagementException, IOException {
        SSLContext context = SSLContext.getDefault();
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        if (trustAll) {
            context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, null);
            factory = context.getSocketFactory();
        }
        WebSocketClient webSocketClient =  new WebSocketClient(url, new Draft_6455(), null, 2000) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                ButtplugWSClient.this.bpLogger.debug("onOpen()");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ButtplugWSClient.this.onConnect();
                        } catch (Exception e) {
                            ButtplugWSClient.this.onError(e);
                        }
                    }
                }).start();
            }

            @Override
            public void onMessage(final String message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ButtplugWSClient.this.onMessage(message);
                    }
                }).start();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ButtplugWSClient.this.disconnect();
                    }
                }).start();
            }

            @Override
            public void onError(final Exception ex) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ButtplugWSClient.this.onError(ex);
                    }
                }).start();
            }
        };
        webSocketClient.setSocket(factory.createSocket());
        webSocketClient.connect();
        return webSocketClient;
    }
}
