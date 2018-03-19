package org.metafetish.buttplug.server.managers.androidbluetoothmanager;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.server.bluetooth.BluetoothSubtypeManager;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AndroidBluetoothManager extends BluetoothSubtypeManager {
    private static final String TAG = AndroidBluetoothManager.class.getSimpleName();
    private static final int REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 5000;

    private Activity activity;
    private Handler handler;
    private boolean scanning;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> bluetoothDevices;

    @NonNull
    private List<AndroidBluetoothDeviceFactory> deviceFactories = new ArrayList<>();
    @NonNull
    public List<String> currentlyConnecting;

    public AndroidBluetoothManager(Activity activity, IButtplugLogManager logManager) {
        super(logManager);

        Log.d(TAG, "Loading Android Bluetooth Manager");
        this.activity = activity;
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AndroidBluetoothManager.this.handler = new Handler();
            }
        });
        this.currentlyConnecting = new ArrayList<>();

        // Introspect the ButtplugDevices namespace for all Factory classes, then create
        // instances of all of them.
        for (IBluetoothDeviceInfo deviceFactory : this.builtinDevices) {
            Log.d(TAG, "Loading Bluetooth Device Factory: " + deviceFactory.getClass()
                    .getSimpleName());
            AndroidBluetoothDeviceFactory androidDeviceFactory = new AndroidBluetoothDeviceFactory
                    (activity, this.bpLogManager, deviceFactory);
            androidDeviceFactory.getDeviceCreated().addCallback(new IButtplugCallback() {
                @Override
                public void invoke(ButtplugEvent event) {
                    IButtplugDevice device = event.getDevice();
                    if (device != null) {
                        Log.d(TAG, "Device created (" + device.getIdentifier() + ")");
                        AndroidBluetoothManager.this.getDeviceAdded().invoke(new ButtplugEvent
                                (device));
                        AndroidBluetoothManager.this.currentlyConnecting.remove(device
                                .getIdentifier());
                    } else {
                        Log.d(TAG, "Failed to create device (" + event.getString() + ")");
                        AndroidBluetoothManager.this.currentlyConnecting.remove(event.getString());
                    }
                }
            });
            this.deviceFactories.add(androidDeviceFactory);
        }

        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "BLE not supported.");
            return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission
                    .ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
        }

        this.bluetoothManager = (BluetoothManager) activity.getSystemService(Context
                .BLUETOOTH_SERVICE);
        if (this.bluetoothManager == null) {
            Log.e(TAG, "bluetoothManager is null.");
            return;
        }

        this.bluetoothAdapter = this.bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) {
            Log.e(TAG, "bluetoothAdapter is null.");
            return;
        }
        if (!this.bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //TODO: Remove this later
//        startScanning();
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            String btAddr = device.getAddress();

            if (AndroidBluetoothManager.this.currentlyConnecting.contains(btAddr)) {
                return;
            }

            final String advertName = device.getName();
            if (advertName == null) {
                return;
            }

            Log.d(TAG, "BLE device found: " + advertName);

            ParcelUuid[] parcelUuids = device.getUuids();
            if (parcelUuids == null) {
                device.fetchUuidsWithSdp();
                parcelUuids = device.getUuids();
            }

            List<UUID> advertGUIDs = new ArrayList<>();
            if (parcelUuids != null) {
                for (ParcelUuid parcelUuid : device.getUuids()) {
                    advertGUIDs.add(parcelUuid.getUuid());
                }
            } else {
                Log.d(TAG, "No UUIDs found: " + advertName);
            }

            List<AndroidBluetoothDeviceFactory> factories = new ArrayList<>();
            for (AndroidBluetoothDeviceFactory factory : AndroidBluetoothManager.this
                    .deviceFactories) {
                if (factory.mayBeDevice(advertName, advertGUIDs)) {
                    factories.add(factory);
                }
            }
            if (factories.size() != 1) {
                if (!factories.isEmpty()) {
                    Log.d(TAG, "Found multiple BLE factories for: " + advertName);
                } else {
                    Log.d(TAG, "No BLE factories found for device: " + advertName);
                }
                return;
            }

            AndroidBluetoothManager.this.currentlyConnecting.add(btAddr);

            AndroidBluetoothDeviceFactory factory = factories.get(0);
            Log.d(TAG, "Found BLE factory: " + factory.getClass().getSimpleName());

            // If we actually have a factory for this device, go ahead and create the device
            try {
                factory.createDeviceAsync(device);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void startScanning() {
        Log.d(TAG, "Starting BLE Scanning");
        if (!this.scanning) {
            this.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AndroidBluetoothManager.this.stopScanning();
                }
            }, SCAN_PERIOD);

            this.scanning = true;
            this.bluetoothAdapter.startLeScan(this.leScanCallback);
            Log.d(TAG, "Started BLE Scanning");
        } else {
            Log.d(TAG, "BLE already Scanning");
        }
    }

    @Override
    public void stopScanning() {
        Log.d(TAG, "Stopping BLE Scanning");
        if (this.scanning) {
            this.scanning = false;
            this.bluetoothAdapter.stopLeScan(this.leScanCallback);
            Log.d(TAG, "Stopped BLE Scanning");
            this.getScanningFinished().invoke(new ButtplugEvent());
        } else {
            Log.d(TAG, "BLE not Scanning");
        }
    }

    @Override
    public boolean isScanning() {
        return this.scanning;
    }

}
