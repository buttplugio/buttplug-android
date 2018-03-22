package org.metafetish.buttplug.apps.bluetoothdeviceemulator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.server.bluetooth.devices.FleshlightLaunchBluetoothInfo;
import org.metafetish.buttplug.server.util.FleshlightHelper;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

    private TextView status;
    private ImageView icon;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothGattCharacteristic tx;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseSettings advertiseSettings;
    private AdvertiseCallback advertiseCallback;
    private AdvertiseData advertiseData;
    private double lastPosition = 10;
    private byte[] lastCommand;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.status = findViewById(R.id.status);
        this.icon = findViewById(R.id.icon);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context
                .BLUETOOTH_SERVICE);

        if (bluetoothManager != null) {
            this.bpLogger.trace("openGattServer()");
            this.bluetoothGattServer = bluetoothManager.openGattServer(this, new
                    BluetoothGattServerCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothDevice device, int status, int
                                newState) {
                            super.onConnectionStateChange(device, status, newState);
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                MainActivity.this.status.setText(R.string.gatt_connected);
                                MainActivity.this.bpLogger.trace("onConnectionStateChange(): STATE_CONNECTED");
                                MainActivity.this.bluetoothLeAdvertiser.stopAdvertising(MainActivity
                                        .this.advertiseCallback);
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                MainActivity.this.status.setText(R.string.advertising_start);
                                MainActivity.this.bpLogger.trace("onConnectionStateChange(): STATE_DISCONNECTED");
                                MainActivity.this.bluetoothLeAdvertiser.startAdvertising(
                                        MainActivity.this.advertiseSettings,
                                        MainActivity.this.advertiseData,
                                        MainActivity.this.advertiseCallback);
                            }
                        }

                        @Override
                        public void onCharacteristicWriteRequest(BluetoothDevice device, int
                                requestId,
                                                                 BluetoothGattCharacteristic
                                                                         characteristic, boolean
                                                                         preparedWrite, boolean
                                                                         responseNeeded, int offset,
                                                                 byte[] value) {
                            super.onCharacteristicWriteRequest(device, requestId, characteristic,
                                    preparedWrite, responseNeeded, offset, value);
                            if (characteristic == MainActivity.this.tx) {
                                MainActivity.this.bpLogger.trace(String.format(
                                        "onCharacteristicWriteRequest(): %s",
                                        Arrays.toString(value)));
                                if (MainActivity.this.lastCommand != null) {
                                    MainActivity.this.lastPosition = MainActivity.this
                                            .lastCommand[0];
                                }
                                MainActivity.this.lastCommand = value;
                                MainActivity.this.animateIcon();
                            }
                            MainActivity.this.bluetoothGattServer.sendResponse(device, requestId,
                                    BluetoothGatt.GATT_SUCCESS, offset, value);
                        }
                    });
            FleshlightLaunchBluetoothInfo deviceInfo = new FleshlightLaunchBluetoothInfo();

            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.setName(deviceInfo.getNames().get(0));

            BluetoothGattService service = new BluetoothGattService(
                    deviceInfo.getServices().get(0),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);
            this.tx = new BluetoothGattCharacteristic(
                    deviceInfo.getCharacteristics().get(FleshlightLaunchBluetoothInfo.Chrs.Tx
                            .ordinal()),
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
            BluetoothGattCharacteristic rx = new BluetoothGattCharacteristic(
                    deviceInfo.getCharacteristics().get(FleshlightLaunchBluetoothInfo.Chrs.Rx
                            .ordinal()),
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            BluetoothGattCharacteristic cmd = new BluetoothGattCharacteristic(
                    deviceInfo.getCharacteristics().get(FleshlightLaunchBluetoothInfo.Chrs.Cmd
                            .ordinal()),
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
            service.addCharacteristic(this.tx);
            service.addCharacteristic(rx);
            service.addCharacteristic(cmd);

            this.bluetoothGattServer.addService(service);

            this.bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
            settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
            this.advertiseSettings = settingsBuilder.build();

            AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
            dataBuilder.setIncludeDeviceName(true);
            this.advertiseData = dataBuilder.build();

            if (this.bluetoothLeAdvertiser != null) {
                this.status.setText(R.string.advertising_start);
                this.bpLogger.trace("startAdvertising()");
                this.advertiseCallback = new AdvertiseCallback() {
                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);

                        if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
                            MainActivity.this.status.setText(R.string.advertising_success);
                            MainActivity.this.bpLogger.trace("onStartFailure(): Already started");
                        } else {
                            MainActivity.this.status.setText(R.string.advertising_failure);
                            MainActivity.this.bluetoothLeAdvertiser.stopAdvertising(this);
                        }
                    }

                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        MainActivity.this.status.setText(R.string.advertising_success);
                        MainActivity.this.bpLogger.trace("onStartSuccess()");
                    }
                };
                this.bluetoothLeAdvertiser.startAdvertising(
                        this.advertiseSettings,
                        this.advertiseData,
                        this.advertiseCallback
                );
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.bpLogger.trace("onDestroy()");
        this.bluetoothLeAdvertiser.stopAdvertising(MainActivity.this.advertiseCallback);
        this.bluetoothGattServer.close();
    }

    private boolean backPressed = false;

    @Override
    public void onBackPressed() {
        if (this.backPressed) {
            super.onBackPressed();
            this.finish();
        } else {
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();
            this.backPressed = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.backPressed = false;
                }
            }, 2000);
        }
    }

    private void animateIcon() {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                double newPosition = MainActivity.this.lastCommand[0];
                double speed = MainActivity.this.lastCommand[1];
                long duration = (long) FleshlightHelper.getDuration(Math.abs(newPosition / 100 -
                        MainActivity.this.lastPosition / 100), speed / 100);
                MainActivity.this.icon.animate().setDuration(duration).translationY(MainActivity
                        .this.fromDp(newPosition));
            }
        });
    }

    private float fromDp(double dp) {
        return (float) (dp * this.getResources().getDisplayMetrics().density);
    }
}
