package org.metafetish.buttplug.client;

import com.google.common.util.concurrent.SettableFuture;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugJsonMessageParser;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.Messages.*;
import org.metafetish.buttplug.core.Messages.Error;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class ButtplugWSClient {

    private ButtplugJsonMessageParser _parser;

    //private IButtplugLog _bpLogger;

    //private IButtplugLogManager _bpLogManager;

    private Object sendLock = new Object();

    private String _clientName;

    private int _messageSchemaVersion;

    private ConcurrentHashMap<Long, SettableFuture<ButtplugMessage>> _waitingMsgs = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, ButtplugClientDevice> _devices = new ConcurrentHashMap<>();

    private WebSocketClient client;

    private Session session;

    private Timer _pingTimer;

    private static final ExecutorService threadpool = Executors.newFixedThreadPool(3);

    //private Task _readThread;

    //private CancellationTokenSource _tokenSource;

    //private Dispatcher _owningDispatcher;

   /* public event EventHandler<DeviceEventArgs>DeviceAdded;

    public event EventHandler<DeviceEventArgs>DeviceRemoved;

    public event EventHandler<ScanningFinishedEventArgs>ScanningFinished;

    public event EventHandler<ErrorEventArgs>ErrorReceived;

    public event EventHandler<LogEventArgs>Log;*/

    private AtomicLong msgId = new AtomicLong(1);

    public ButtplugWSClient(String aClientName) {
        _clientName = aClientName;
        //_bpLogManager = new ButtplugLogManager();
        //_bpLogger = _bpLogManager.GetLogger(GetType());
        _parser = new ButtplugJsonMessageParser();
        //_bpLogger.Info("Finished setting up ButtplugClient");
        //_owningDispatcher = Dispatcher.CurrentDispatcher;
        //_tokenSource = new CancellationTokenSource();
    }

    public void Connect(URI url) throws Exception {

        //if (_ws != null && (_ws.State == WebSocketState.Connecting || _ws.State == WebSocketState.Open)) {
        //    throw new AccessViolationException("Already connected!");
        //}

        client = new WebSocketClient();


        _waitingMsgs.clear();
        _devices.clear();
        msgId.set(1);

        try {
            client.start();
            client.connect(this, url, new ClientUpgradeRequest()).get();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        //if (_ws.State != WebSocketState.Open) {
        //    throw new Exception("Connection failed!");
        //}

        ButtplugMessage res = SendMessage(new RequestServerInfo(_clientName)).get();
        if (res instanceof ServerInfo) {
            if (((ServerInfo) res).maxPingTime > 0) {
                _pingTimer = new Timer("pingTimer", true);
                //onPingTimer, null, 0,  0)));
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
            throw new Exception(((org.metafetish.buttplug.core.Messages.Error) res).getErrorMessage());
        } else {
            throw new Exception("Unexpecte message returned: " + res.getClass().getName());
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        this.session = null;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
    }

    public void Disconnect() {
        if (_pingTimer != null) {
            _pingTimer.cancel();
            _pingTimer = null;
        }

        if (session != null) {
            try {
                session.disconnect();
            } catch (IOException e) {
                // noop - something when wrong closing the socket, but we're
                // about to dispose of it anyway.
            }
        }
        client = null;

        int max = 3;
        while (max-- > 0 && _waitingMsgs.size() != 0) {
            for (long msgId : _waitingMsgs.keySet()) {
                SettableFuture<ButtplugMessage> val = _waitingMsgs.remove(msgId);
                if (val != null) {
                    val.set(new Error("Connection closed!", Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.SystemMsgId));
                }
            }
        }

        msgId.set(1);
    }

    @OnWebSocketMessage
    public void onMessage(String buf) {
        try {
            List<ButtplugMessage> msgs = _parser.parseJson(buf);

            for (ButtplugMessage msg : msgs) {
                if (msg.id > 0) {
                    SettableFuture<ButtplugMessage> val = _waitingMsgs.remove(msg.id);
                    if (val != null) {
                        val.set(msg);
                        continue;
                    }
                }

                if (msg instanceof Log) {
                    // _owningDispatcher.Invoke(() = >
                    //         {
                    //                 Log ?.Invoke(this, new LogEventArgs(l));
                    //                 });
                } else if (msg instanceof DeviceAdded) {

                    //   var dev = new ButtplugClientDevice(d);
                    //  _devices.AddOrUpdate(d.DeviceIndex, dev, (idx, old) =>dev);
                    //  _owningDispatcher.Invoke(() = >
                    //          {
                    //                 DeviceAdded ?.Invoke(this, new DeviceEventArgs(dev, DeviceAction.ADDED));
                    //                 });
                } else if (msg instanceof DeviceRemoved) {
                    // if (_devices.TryRemove(d.DeviceIndex, out ButtplugClientDevice oldDev)) {
                    //   _owningDispatcher.Invoke(() = >
                    //           {
                    //                   DeviceRemoved ?.Invoke(this, new DeviceEventArgs(oldDev, DeviceAction.REMOVED));
                    //                    });
                    // }

                } else if (msg instanceof ScanningFinished) {

                    //_owningDispatcher.Invoke(() = >
                    //         {
                    //               ScanningFinished ?.Invoke(this, new ScanningFinishedEventArgs(sf));
                    //             });
                } else if (msg instanceof org.metafetish.buttplug.core.Messages.Error) {
                    //_owningDispatcher.Invoke(() = >
                    //         {
                    //                ErrorReceived ?.Invoke(this, new ErrorEventArgs(e));
                    //                });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onPingTimer() throws Exception {
        try {
            ButtplugMessage msg = SendMessage(new Ping(msgId.incrementAndGet())).get();
            if (msg instanceof org.metafetish.buttplug.core.Messages.Error) {
                throw new Exception(((org.metafetish.buttplug.core.Messages.Error) msg).getErrorMessage());
            }
        } catch (Throwable e) {
            if (client != null) {
                Disconnect();
            }
            throw e;
        }
    }

    public void RequestDeviceList() throws ExecutionException, InterruptedException, IOException {
        ButtplugMessage res = SendMessage(new RequestDeviceList(msgId.incrementAndGet())).get();
        if (!(res instanceof DeviceList) || ((DeviceList) res).devices == null) {
            if (res instanceof org.metafetish.buttplug.core.Messages.Error) {
                // _owningDispatcher.Invoke(() = >
                //         {
                //                 ErrorReceived ?.Invoke(this, new ErrorEventArgs(resp as org.metafetish.buttplug.core.Messages.Error));
                //     });
            }

            return;
        }

        for (DeviceMessageInfo d : ((DeviceList) res).devices) {
            if (!_devices.containsKey(d.deviceIndex)) {
                ButtplugClientDevice device = new ButtplugClientDevice(d);
                if (_devices.put(d.deviceIndex, device) == null) {
                    // _owningDispatcher.Invoke(() = >
                    //         {
                    //                 DeviceAdded ?.Invoke(this, new DeviceEventArgs(device, DeviceAction.ADDED));
                    //    });
                }
            }
        }
    }

    public List<ButtplugClientDevice> getDevices() {
        List<ButtplugClientDevice> devices = new ArrayList<>();
        devices.addAll(_devices.values());
        return devices;
    }

    public boolean StartScanning() throws ExecutionException, InterruptedException, IOException {
        return SendMessageExpectOk(new StartScanning(msgId.incrementAndGet()));
    }

    public boolean StopScanning() throws ExecutionException, InterruptedException, IOException {
        return SendMessageExpectOk(new StopScanning(msgId.incrementAndGet()));
    }

    public boolean RequestLog(RequestLog.ButtplugLogLevel aLogLevel) throws ExecutionException, InterruptedException, IOException {
        return SendMessageExpectOk(new RequestLog(aLogLevel, msgId.getAndIncrement()));
    }

    public SettableFuture<ButtplugMessage> SendDeviceMessage(ButtplugClientDevice device, ButtplugDeviceMessage aDeviceMsg) throws ExecutionException, InterruptedException, IOException {
        SettableFuture<ButtplugMessage> promise = SettableFuture.create();
        ButtplugClientDevice dev = _devices.get(device.index);
        if (dev != null) {
            if (!dev.allowedMessages.contains(aDeviceMsg.getClass().getSimpleName())) {
                return null;//new org.metafetish.buttplug.core.Messages.Error("Device does not accept message type: " + aDeviceMsg.getClass().getSimpleName(), org.metafetish.buttplug.core.Messages.Error.ErrorClass.ERROR_DEVICE, ButtplugConsts.SystemMsgId);
            }

            aDeviceMsg.deviceIndex = device.index;
            return SendMessage(aDeviceMsg);
        } else {
            return null;//new org.metafetish.buttplug.core.Messages.Error("Device not available.", org.metafetish.buttplug.core.Messages.Error.ErrorClass.ERROR_DEVICE, ButtplugConsts.SystemMsgId);
        }
    }

    protected boolean SendMessageExpectOk(ButtplugMessage aMsg) throws ExecutionException, InterruptedException, IOException {
        return SendMessage(aMsg).get() instanceof Ok;
    }


    protected SettableFuture<ButtplugMessage> SendMessage(ButtplugMessage aMsg) throws ExecutionException, InterruptedException, IOException {
        SettableFuture<ButtplugMessage> promise = SettableFuture.create();

        _waitingMsgs.put(aMsg.id, promise);
        if (session == null) {
            promise.set(new org.metafetish.buttplug.core.Messages.Error("Bad WS state!", Error.ErrorClass.ERROR_UNKNOWN, ButtplugConsts.SystemMsgId));
            return promise;
        }

        try {
            Future<Void> fut = session.getRemote().sendStringByFuture(_parser.formatJson(aMsg));
            fut.get();
        } catch (WebSocketException e) {
            promise.set(new org.metafetish.buttplug.core.Messages.Error(e.getMessage(), org.metafetish.buttplug.core.Messages.Error.ErrorClass.ERROR_UNKNOWN, aMsg.id));
        }

        return promise;
    }
}