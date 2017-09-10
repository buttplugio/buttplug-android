package org.metafetish.buttplug.client;

import com.neovisionaries.ws.client.*;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.Messages.*;
import org.metafetish.buttplug.core.Messages.Error;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class ButtplugWSClient extends WebSocketAdapter {

    public IDeviceEvent deviceAdded;

    public IDeviceEvent deviceRemoved;
    public IScanningEvent scanningFinished;
    public IErrorEvent erorReceived;
    public ILogEvent logReceived;

    private WebSocket websocket;
    private ButtplugJsonMessageParser _parser;
    private Object sendLock = new Object();
    private String _clientName;
    private int _messageSchemaVersion;
    private ConcurrentHashMap<Long, SettableListenableFuture<ButtplugMessage>> _waitingMsgs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ButtplugClientDevice> _devices = new ConcurrentHashMap<>();

    private Timer _pingTimer;
    private AtomicLong msgId = new AtomicLong(1);

    public ButtplugWSClient(String aClientName) {
        _clientName = aClientName;
        _parser = new ButtplugJsonMessageParser();
    }

    public long getNextMsgId() {
        return msgId.getAndIncrement();
    }

    public void Connect(URI url) throws Exception {
        Connect(url, false);
    }

    public void Connect(URI url, boolean trustAll) throws Exception {

        _waitingMsgs.clear();
        _devices.clear();
        msgId.set(1);

        websocket = getWebSocket(url, trustAll);

        ButtplugMessage res = sendMessage(new RequestServerInfo(_clientName, getNextMsgId())).get();
        if (res instanceof ServerInfo) {
            if (((ServerInfo) res).maxPingTime > 0) {
                _pingTimer = new Timer("pingTimer", true);
                _pingTimer.scheduleAtFixedRate(new TimerTask() {
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

        } else if (res instanceof org.metafetish.buttplug.core.Messages.Error) {
            throw new Exception(((org.metafetish.buttplug.core.Messages.Error) res).errorMessage);
        } else {
            throw new Exception("Unexpected message returned: " + res.getClass().getName());
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
        if (_pingTimer != null) {
            _pingTimer.cancel();
            _pingTimer = null;
        }

        if (websocket != null) {
            websocket.disconnect();
            websocket = null;
        }

        int max = 3;
        while (max-- > 0 && _waitingMsgs.size() != 0) {
            for (long msgId : _waitingMsgs.keySet()) {
                SettableListenableFuture<ButtplugMessage> val = _waitingMsgs.remove(msgId);
                if (val != null) {
                    val.set(new Error("Connection closed!", Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.SystemMsgId));
                }
            }
        }
        msgId.set(1);
    }

    @Override
    public void onTextMessage(WebSocket socket, String buf) {
        try {
            List<ButtplugMessage> msgs = _parser.parseJson(buf);

            for (ButtplugMessage msg : msgs) {
                if (msg.id > 0) {
                    SettableListenableFuture<ButtplugMessage> val = _waitingMsgs.remove(msg.id);
                    if (val != null) {
                        val.set(msg);
                        continue;
                    }
                }

                if (msg instanceof Log) {
                    if (logReceived != null) {
                        logReceived.logReceived((Log) msg);
                    }
                } else if (msg instanceof DeviceAdded) {
                    ButtplugClientDevice device = new ButtplugClientDevice((DeviceAdded) msg);
                    _devices.put(((DeviceAdded) msg).deviceIndex, device);
                    if (deviceAdded != null) {
                        deviceAdded.deviceAdded(device);
                    }
                } else if (msg instanceof DeviceRemoved) {
                    if (_devices.remove(((DeviceRemoved) msg).deviceIndex) != null) {
                        if (deviceRemoved != null) {
                            deviceRemoved.deviceRemoved(((DeviceRemoved) msg).deviceIndex);
                        }
                    }
                } else if (msg instanceof ScanningFinished) {
                    if (scanningFinished != null) {
                        scanningFinished.scanningFinished();
                    }
                } else if (msg instanceof org.metafetish.buttplug.core.Messages.Error) {
                    if (erorReceived != null) {
                        erorReceived.errorReceived((org.metafetish.buttplug.core.Messages.Error) msg);
                    }
                }
            }
        } catch (IOException e) {
            if (erorReceived != null) {
                erorReceived.errorReceived(new org.metafetish.buttplug.core.Messages.Error(e.getMessage(),
                        Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.SystemMsgId));
            } else {
                e.printStackTrace();
            }
        }
    }

    private void onPingTimer() throws Exception {
        try {
            ButtplugMessage msg = sendMessage(new Ping(msgId.incrementAndGet())).get();
            if (msg instanceof org.metafetish.buttplug.core.Messages.Error) {
                throw new Exception(((org.metafetish.buttplug.core.Messages.Error) msg).errorMessage);
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
            if (res instanceof org.metafetish.buttplug.core.Messages.Error) {
                throw new Exception(((org.metafetish.buttplug.core.Messages.Error) res).errorMessage);
            }
            return;
        }

        for (DeviceMessageInfo d : ((DeviceList) res).devices) {
            if (!_devices.containsKey(d.deviceIndex)) {
                ButtplugClientDevice device = new ButtplugClientDevice(d);
                if (_devices.put(d.deviceIndex, device) == null) {
                    if (deviceAdded != null) {
                        deviceAdded.deviceAdded(device);
                    }
                }
            }
        }
    }

    public List<ButtplugClientDevice> getDevices() {
        List<ButtplugClientDevice> devices = new ArrayList<>();
        devices.addAll(_devices.values());
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

    public boolean requestLog(RequestLog.ButtplugLogLevel aLogLevel) throws ExecutionException, InterruptedException, IOException {
        return sendMessageExpectOk(new RequestLog(aLogLevel, msgId.getAndIncrement()));
    }

    public ListenableFuture<ButtplugMessage> sendDeviceMessage(ButtplugClientDevice device, ButtplugDeviceMessage deviceMsg) throws ExecutionException, InterruptedException, IOException {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<ButtplugMessage>();
        ButtplugClientDevice dev = _devices.get(device.index);
        if (dev != null) {
            if (!dev.allowedMessages.contains(deviceMsg.getClass().getSimpleName())) {
                promise.set(new org.metafetish.buttplug.core.Messages.Error("Device does not accept message type: " + deviceMsg.getClass().getSimpleName(), org.metafetish.buttplug.core.Messages.Error.ErrorClass.ERROR_DEVICE, ButtplugConsts.SystemMsgId));
                return promise;
            }

            deviceMsg.deviceIndex = device.index;
            deviceMsg.id = msgId.incrementAndGet();
            return sendMessage(deviceMsg);
        } else {
            promise.set(new org.metafetish.buttplug.core.Messages.Error("Device not available.", org.metafetish.buttplug.core.Messages.Error.ErrorClass.ERROR_DEVICE, ButtplugConsts.SystemMsgId));
            return promise;
        }
    }

    protected boolean sendMessageExpectOk(ButtplugMessage msg) throws ExecutionException, InterruptedException, IOException {
        return sendMessage(msg).get() instanceof Ok;
    }


    protected ListenableFuture<ButtplugMessage> sendMessage(ButtplugMessage msg) throws ExecutionException, InterruptedException, IOException {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<ButtplugMessage>();

        _waitingMsgs.put(msg.id, promise);
        if (websocket == null) {
            promise.set(new org.metafetish.buttplug.core.Messages.Error("Bad WS state!", Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.SystemMsgId));
            return promise;
        }

        try {
            websocket.sendText(_parser.formatJson(msg));
            websocket.flush();
        } catch (IOException e) {
            promise.set(new org.metafetish.buttplug.core.Messages.Error(e.getMessage(), org.metafetish.buttplug.core.Messages.Error.ErrorClass.ERROR_UNKNOWN, msg.id));
        }

        return promise;
    }

    protected WebSocket getWebSocket(URI url, boolean trustAll) throws IOException, WebSocketException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getDefault();
        if(trustAll)
        {
            context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType)
                {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                {
                }
            } }, null);
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