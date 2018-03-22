package org.metafetish.buttplug.client;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugLogLevel;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.Messages.DeviceAdded;
import org.metafetish.buttplug.core.Messages.DeviceList;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.DeviceRemoved;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.Log;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.Ping;
import org.metafetish.buttplug.core.Messages.RequestDeviceList;
import org.metafetish.buttplug.core.Messages.RequestLog;
import org.metafetish.buttplug.core.Messages.RequestServerInfo;
import org.metafetish.buttplug.core.Messages.ScanningFinished;
import org.metafetish.buttplug.core.Messages.ServerInfo;
import org.metafetish.buttplug.core.Messages.StartScanning;
import org.metafetish.buttplug.core.Messages.StopAllDevices;
import org.metafetish.buttplug.core.Messages.StopScanning;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ButtplugWSClient extends WebSocketAdapter {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

    public IDeviceEvent deviceAdded;

    public IDeviceEvent deviceRemoved;
    public IScanningEvent scanningFinished;
    public IErrorEvent erorReceived;
    public ILogEvent logReceived;

    private WebSocket websocket;
    private ButtplugJsonMessageParser parser;
    private Object sendLock = new Object();
    private String clientName;
    private int messageSchemaVersion;
    private ConcurrentHashMap<Long, SettableListenableFuture<ButtplugMessage>> waitingMsgs = new
            ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ButtplugClientDevice> devices = new ConcurrentHashMap<>();

    private Timer pingTimer;
    private AtomicLong msgId = new AtomicLong(1);

    public ButtplugWSClient(String clientName) {
        this.clientName = clientName;
        this.parser = new ButtplugJsonMessageParser();
    }

    public long getNextMsgId() {
        return msgId.getAndIncrement();
    }

    public void Connect(URI url) throws Exception {
        Connect(url, false);
    }

    public void Connect(URI url, boolean trustAll) throws Exception {

        this.waitingMsgs.clear();
        this.devices.clear();
        this.msgId.set(1);

        this.websocket = getWebSocket(url, trustAll);

        ButtplugMessage res = sendMessage(new RequestServerInfo(this.clientName, getNextMsgId(), 0))
                .get();
        if (res instanceof ServerInfo) {
            if (((ServerInfo) res).maxPingTime > 0) {
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
                }, 0, Math.round(((double) ((ServerInfo) res).maxPingTime) / 2));
            }

        } else if (res instanceof Error) {
            throw new Exception(((Error) res).errorMessage);
        } else {
            throw new Exception(String.format("Unexpected message returned: %s", res.getClass().getName()));
        }
    }

    /*
    public void onClose(int statusCode, String reason) {
        this.session = null;
    }

    //
    public void onConnect(Session session) {
        this.session = session;
    }
*/
    public void Disconnect() {
        if (this.pingTimer != null) {
            this.pingTimer.cancel();
            this.pingTimer = null;
        }

        if (this.websocket != null) {
            this.websocket.disconnect();
            this.websocket = null;
        }

        int max = 3;
        while (max-- > 0 && this.waitingMsgs.size() != 0) {
            for (long msgId : this.waitingMsgs.keySet()) {
                SettableListenableFuture<ButtplugMessage> val = this.waitingMsgs.remove(msgId);
                if (val != null) {
                    val.set(new Error("Connection closed!", Error.ErrorClass.ERROR_UNKNOWN,
                            ButtplugConsts.SystemMsgId));
                }
            }
        }
        msgId.set(1);
    }

    @Override
    public void onTextMessage(WebSocket socket, String buf) {
        try {
            List<ButtplugMessage> msgs = this.parser.deserialize(buf);

            for (ButtplugMessage msg : msgs) {
                if (msg.id > 0) {
                    SettableListenableFuture<ButtplugMessage> val = this.waitingMsgs.remove(msg.id);
                    if (val != null) {
                        val.set(msg);
                        continue;
                    }
                }

                if (msg instanceof Log) {
                    if (this.logReceived != null) {
                        this.logReceived.logReceived((Log) msg);
                    }
                } else if (msg instanceof DeviceAdded) {
                    ButtplugClientDevice device = new ButtplugClientDevice((DeviceAdded) msg);
                    this.devices.put(((DeviceAdded) msg).deviceIndex, device);
                    if (this.deviceAdded != null) {
                        this.deviceAdded.deviceAdded(device);
                    }
                } else if (msg instanceof DeviceRemoved) {
                    if (this.devices.remove(((DeviceRemoved) msg).deviceIndex) != null) {
                        if (this.deviceRemoved != null) {
                            this.deviceRemoved.deviceRemoved(((DeviceRemoved) msg).deviceIndex);
                        }
                    }
                } else if (msg instanceof ScanningFinished) {
                    if (this.scanningFinished != null) {
                        this.scanningFinished.scanningFinished();
                    }
                } else if (msg instanceof Error) {
                    if (this.erorReceived != null) {
                        this.erorReceived.errorReceived((Error) msg);
                    }
                }
            }
        } catch (IOException e) {
            if (this.erorReceived != null) {
                this.erorReceived.errorReceived(new Error(e.getMessage(),
                        Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.SystemMsgId));
            } else {
                e.printStackTrace();
            }
        }
    }

    private void onPingTimer() throws Exception {
        try {
            ButtplugMessage msg = sendMessage(new Ping(msgId.incrementAndGet())).get();
            if (msg instanceof Error) {
                throw new Exception(((Error) msg).errorMessage);
            }
        } catch (Throwable e) {
            if (websocket != null) {
                websocket.disconnect();
                websocket = null;
            }
            throw e;
        }
    }

    public void requestDeviceList() throws Exception {
        ButtplugMessage res = sendMessage(new RequestDeviceList(msgId.incrementAndGet())).get();
        if (!(res instanceof DeviceList) || ((DeviceList) res).devices == null) {
            if (res instanceof Error) {
                throw new Exception(((Error) res).errorMessage);
            }
            return;
        }

        for (DeviceMessageInfo d : ((DeviceList) res).devices) {
            if (!this.devices.containsKey(d.deviceIndex)) {
                ButtplugClientDevice device = new ButtplugClientDevice(d);
                if (this.devices.put(d.deviceIndex, device) == null) {
                    if (this.deviceAdded != null) {
                        this.deviceAdded.deviceAdded(device);
                    }
                }
            }
        }
    }

    public List<ButtplugClientDevice> getDevices() {
        List<ButtplugClientDevice> devices = new ArrayList<>();
        devices.addAll(this.devices.values());
        return devices;
    }

    public boolean startScanning() throws ExecutionException, InterruptedException, IOException {
        return sendMessageExpectOk(new StartScanning(msgId.incrementAndGet()));
    }

    public boolean stopScanning() throws ExecutionException, InterruptedException, IOException {
        return sendMessageExpectOk(new StopScanning(msgId.incrementAndGet()));
    }

    public boolean stopAllDevices() throws ExecutionException, InterruptedException, IOException {
        return sendMessageExpectOk(new StopAllDevices(msgId.incrementAndGet()));
    }

    public boolean requestLog(ButtplugLogLevel logLevel) throws ExecutionException,
            InterruptedException, IOException {
        return sendMessageExpectOk(new RequestLog(logLevel, msgId.getAndIncrement()));
    }

    public ListenableFuture<ButtplugMessage> sendDeviceMessage(ButtplugClientDevice device,
                                                               ButtplugDeviceMessage deviceMsg)
            throws ExecutionException, InterruptedException, IOException {
        SettableListenableFuture<ButtplugMessage> promise = new
                SettableListenableFuture<ButtplugMessage>();
        ButtplugClientDevice dev = this.devices.get(device.index);
        if (dev != null) {
            if (!dev.allowedMessages.contains(deviceMsg.getClass().getSimpleName())) {
                promise.set(new Error(String.format("Device does not accept message type: %s",
                        deviceMsg.getClass().getSimpleName()),
                        Error.ErrorClass.ERROR_DEVICE,
                        ButtplugConsts.SystemMsgId));
                return promise;
            }

            deviceMsg.deviceIndex = device.index;
            deviceMsg.id = this.msgId.incrementAndGet();
            return sendMessage(deviceMsg);
        } else {
            promise.set(new Error("Device not available.", Error.ErrorClass.ERROR_DEVICE,
                    ButtplugConsts.SystemMsgId));
            return promise;
        }
    }

    protected boolean sendMessageExpectOk(ButtplugMessage msg) throws ExecutionException,
            InterruptedException, IOException {
        return sendMessage(msg).get() instanceof Ok;
    }


    protected ListenableFuture<ButtplugMessage> sendMessage(ButtplugMessage msg) throws
            ExecutionException, InterruptedException, IOException {
        SettableListenableFuture<ButtplugMessage> promise = new
                SettableListenableFuture<ButtplugMessage>();

        this.waitingMsgs.put(msg.id, promise);
        if (this.websocket == null) {
            promise.set(new Error("Bad WS state!", Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts
                    .SystemMsgId));
            return promise;
        }

        try {
            //TODO: Upgrade client to schema version 1
            this.websocket.sendText(this.parser.serialize(msg, 0));
            this.websocket.flush();
        } catch (IOException e) {
            promise.set(new Error(e.getMessage(), Error.ErrorClass.ERROR_UNKNOWN, msg.id));
        }

        return promise;
    }

    protected WebSocket getWebSocket(URI url, boolean trustAll) throws IOException,
            WebSocketException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getDefault();
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
        }
        return new WebSocketFactory()
                .setSSLContext(context)
                .setConnectionTimeout(2000)
                .createSocket(url)
                .addListener(this)
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect();
    }
}