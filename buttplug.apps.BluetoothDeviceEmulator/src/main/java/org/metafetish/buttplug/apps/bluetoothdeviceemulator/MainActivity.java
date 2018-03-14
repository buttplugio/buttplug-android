package org.metafetish.buttplug.apps.bluetoothdeviceemulator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGatt;
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
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import org.metafetish.buttplug.server.bluetooth.devices.FleshlightLaunchBluetoothInfo;
import org.metafetish.buttplug.server.util.FleshlightHelper;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private TextView mStatus;
    private ImageView mIcon;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothGattCharacteristic mTx;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseSettings mAdvertiseSettings;
    private AdvertiseCallback mAdvertiseCallback;
    private AdvertiseData mAdvertiseData;
    private double mLastPosition = 10;
    private byte[] mLastCommand;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //TODO: Deal with device orientation changes

        this.mStatus = findViewById(R.id.status);
        this.mIcon = findViewById(R.id.icon);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            Log.d(TAG, "openGattServer()");
            this.mBluetoothGattServer = bluetoothManager.openGattServer(this, new BluetoothGattServerCallback() {
                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                    super.onConnectionStateChange(device, status, newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        MainActivity.this.mStatus.setText(R.string.gatt_connected);
                        Log.d(TAG, "onConnectionStateChange(): STATE_CONNECTED");
                        MainActivity.this.mBluetoothLeAdvertiser.stopAdvertising(MainActivity.this.mAdvertiseCallback);
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        MainActivity.this.mStatus.setText(R.string.advertising_start);
                        Log.d(TAG, "onConnectionStateChange(): STATE_DISCONNECTED");
                        MainActivity.this.mBluetoothLeAdvertiser.startAdvertising(
                                MainActivity.this.mAdvertiseSettings,
                                MainActivity.this.mAdvertiseData,
                                MainActivity.this.mAdvertiseCallback);
                    }
                }
                @Override
                public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                    super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                    if (characteristic == MainActivity.this.mTx) {
                        Log.d(TAG, "onCharacteristicWriteRequest(): " + Arrays.toString(value));
                        if (MainActivity.this.mLastCommand != null) {
                            MainActivity.this.mLastPosition = MainActivity.this.mLastCommand[0];
                        }
                        MainActivity.this.mLastCommand = value;
                        MainActivity.this.animateIcon();
                    }
                    MainActivity.this.mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                }
            });
            FleshlightLaunchBluetoothInfo deviceInfo = new FleshlightLaunchBluetoothInfo();

            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.setName(deviceInfo.getNames().get(0));

            BluetoothGattService service = new BluetoothGattService(
                    deviceInfo.getServices().get(0),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);
            this.mTx = new BluetoothGattCharacteristic(
                    deviceInfo.getCharacteristics().get(FleshlightLaunchBluetoothInfo.Chrs.Tx.ordinal()),
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
            BluetoothGattCharacteristic rx = new BluetoothGattCharacteristic(
                    deviceInfo.getCharacteristics().get(FleshlightLaunchBluetoothInfo.Chrs.Rx.ordinal()),
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            BluetoothGattCharacteristic cmd = new BluetoothGattCharacteristic(
                    deviceInfo.getCharacteristics().get(FleshlightLaunchBluetoothInfo.Chrs.Cmd.ordinal()),
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
            service.addCharacteristic(this.mTx);
            service.addCharacteristic(rx);
            service.addCharacteristic(cmd);

            this.mBluetoothGattServer.addService(service);

            this.mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
            settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
            this.mAdvertiseSettings = settingsBuilder.build();

            AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
            dataBuilder.setIncludeDeviceName(true);
            this.mAdvertiseData = dataBuilder.build();

            if (this.mBluetoothLeAdvertiser != null) {
                this.mStatus.setText(R.string.advertising_start);
                Log.d(TAG, "startAdvertising()");
                this.mAdvertiseCallback = new AdvertiseCallback() {
                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);

                        if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
                            MainActivity.this.mStatus.setText(R.string.advertising_success);
                            Log.d(TAG, "onStartFailure(): Already started");
                        } else {
                            MainActivity.this.mStatus.setText(R.string.advertising_failure);
                            MainActivity.this.mBluetoothLeAdvertiser.stopAdvertising(this);
                        }
                    }

                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        MainActivity.this.mStatus.setText(R.string.advertising_success);
                        Log.d(TAG, "onStartSuccess()");
                    }
                };
                this.mBluetoothLeAdvertiser.startAdvertising(
                        this.mAdvertiseSettings,
                        this.mAdvertiseData,
                        this.mAdvertiseCallback
                );
            }
        }
    }

    private void animateIcon() {
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                double newPosition = MainActivity.this.mLastCommand[0];
                double speed = MainActivity.this.mLastCommand[1];
                long duration = (long) FleshlightHelper.GetDuration(Math.abs(newPosition / 100 - MainActivity.this.mLastPosition / 100), speed / 100);
                MainActivity.this.mIcon.animate().setDuration(duration).translationY(MainActivity.this.fromDp(newPosition));
            }
        });
    }

    private float fromDp(double dp) {
        return (float) (dp * this.getResources().getDisplayMetrics().density);
    }
}
