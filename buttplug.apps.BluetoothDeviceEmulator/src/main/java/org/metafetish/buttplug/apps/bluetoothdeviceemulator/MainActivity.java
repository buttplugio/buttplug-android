package org.metafetish.buttplug.apps.bluetoothdeviceemulator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mBluetoothGattServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            Log.d(TAG, "openGattServer()");
            this.mBluetoothGattServer = bluetoothManager.openGattServer(this, new BluetoothGattServerCallback() {
                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                    super.onConnectionStateChange(device, status, newState);
                    Log.d(TAG, "onConnectionStateChange()");
                    Log.d(TAG, "newState: " + newState);
                    //TODO: Stop advertising on connect
                    //TODO: Start advertising on disconnect
                }
            });
            BluetoothGattService service = new BluetoothGattService(UUID.fromString("88f80580-0000-01e6-aace-0002a5d5c51b"), BluetoothGattService.SERVICE_TYPE_PRIMARY);
            BluetoothGattCharacteristic tx = new BluetoothGattCharacteristic(UUID.fromString("88f80581-0000-01e6-aace-0002a5d5c51b"), BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
            BluetoothGattCharacteristic rx = new BluetoothGattCharacteristic(UUID.fromString("88f80582-0000-01e6-aace-0002a5d5c51b"), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
            BluetoothGattCharacteristic cmd = new BluetoothGattCharacteristic(UUID.fromString("88f80583-0000-01e6-aace-0002a5d5c51b"),BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
            service.addCharacteristic(tx);
            service.addCharacteristic(rx);
            service.addCharacteristic(cmd);
            this.mBluetoothGattServer.addService(service);

            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.setName("Launch");
            if (bluetoothAdapter != null) {
                this.mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
                AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
                settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
                AdvertiseSettings settings = settingsBuilder.build();

                AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
                dataBuilder.setIncludeDeviceName(true);
//                dataBuilder.addServiceUuid(new ParcelUuid(UUID.fromString("88f80580-0000-01e6-aace-0002a5d5c51b")));
                AdvertiseData advertiseData = dataBuilder.build();
                if (this.mBluetoothLeAdvertiser != null) {
                    Log.d(TAG, "startAdvertising()");
                    this.mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, new AdvertiseCallback() {
                        @Override
                        public void onStartFailure(int errorCode) {
                            super.onStartFailure(errorCode);

                            Log.d(TAG, "onStartFailure()");
                            MainActivity.this.mBluetoothLeAdvertiser.stopAdvertising(this);
                        }

                        @Override
                        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                            super.onStartSuccess(settingsInEffect);
                            Log.d(TAG, "onStartSuccess()");
                        }
                    });
                }
            }
        }
    }
}
