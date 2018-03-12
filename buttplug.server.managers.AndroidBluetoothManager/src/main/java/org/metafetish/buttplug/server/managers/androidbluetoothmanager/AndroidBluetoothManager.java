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
import org.metafetish.buttplug.core.ButtplugEventHandler;
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
    private Handler mHandler;
    private boolean mScanning;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> mLeDevices;

    @NonNull
    private List<AndroidBluetoothDeviceFactory> deviceFactories = new ArrayList<>();
    @NonNull
    public List<String> currentlyConnecting;

    public AndroidBluetoothManager(Activity activity, IButtplugLogManager aLogManager) {
        super(aLogManager);

        Log.d(TAG, "Loading Android Bluetooth Manager");
        this.activity = activity;

        mHandler = new Handler();

        currentlyConnecting = new ArrayList<>();

        // Introspect the ButtplugDevices namespace for all Factory classes, then create
        // instances of all of them.
        for (IBluetoothDeviceInfo aDeviceFactory : builtinDevices) {
            Log.d(TAG, "Loading Bluetooth Device Factory: " + aDeviceFactory.getClass()
                    .getSimpleName());
            Log.d(TAG, "earlyservices: " + aDeviceFactory.getServices());

            Log.d(TAG, "Loading Bluetooth Device Factory: " + aDeviceFactory.getClass()
                    .getSimpleName());
            AndroidBluetoothDeviceFactory deviceFactory = new AndroidBluetoothDeviceFactory(activity, this.bpLogManager, aDeviceFactory);
            deviceFactory.getDeviceCreated().addCallback(new IButtplugCallback() {
                @Override
                public void invoke(ButtplugEvent aEvent) {
                    IButtplugDevice device = aEvent.getDevice();
                    if (device != null) {
                        Log.d(TAG, "Device created (" + device.getIdentifier() + ")");
                        AndroidBluetoothManager.this.getDeviceAdded().invoke(new ButtplugEvent(device));
                        currentlyConnecting.remove(device.getIdentifier());
                    } else {
                        Log.d(TAG, "Failed to create device (" + aEvent.getString() + ")");
                        currentlyConnecting.remove(aEvent.getString());
                    }
                }
            });
            deviceFactories.add(deviceFactory);
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

        mBluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.e(TAG, "mBluetoothManager is null.");
            return;
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "mBluetoothAdapter is null.");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //TODO: Remove this later
        startScanning();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback
            () {
        @Override
        public void onLeScan(final BluetoothDevice aDevice, int rssi, byte[] scanRecord) {
            String btAddr = aDevice.getAddress();

            if (currentlyConnecting.contains(btAddr)) {
                return;
            }

            final String advertName = aDevice.getName();
            if (advertName == null) {
                return;
            }

            Log.d(TAG, "BLE device found: " + advertName);

            ParcelUuid[] parcelUuids = aDevice.getUuids();
            if (parcelUuids == null) {
                aDevice.fetchUuidsWithSdp();
                parcelUuids = aDevice.getUuids();
            }

            List<UUID> advertGUIDs = new ArrayList<>();
            if (parcelUuids != null) {
                for (ParcelUuid parcelUuid : aDevice.getUuids()) {
                    advertGUIDs.add(parcelUuid.getUuid());
                }
            } else {
                Log.d(TAG, "No UUIDs found: " + advertName);
            }

            List<AndroidBluetoothDeviceFactory> factories = new ArrayList<>();
            for (AndroidBluetoothDeviceFactory factory : deviceFactories) {
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

            currentlyConnecting.add(btAddr);

            AndroidBluetoothDeviceFactory factory = factories.get(0);
            Log.d(TAG, "Found BLE factory: " + factory.getClass().getSimpleName());

            // If we actually have a factory for this device, go ahead and create the device
            IButtplugDevice device = null;
            try {
                factory.createDeviceAsync(aDevice);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void startScanning() {
        Log.d(TAG, "Starting BLE Scanning");
        if (!mScanning) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AndroidBluetoothManager.this.stopScanning();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.d(TAG, "Started BLE Scanning");
        } else {
            Log.d(TAG, "BLE already Scanning");
        }
    }

    @Override
    public void stopScanning() {
        Log.d(TAG, "Stopping BLE Scanning");
        if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.d(TAG, "Stopped BLE Scanning");
            AndroidBluetoothManager.this.getScanningFinished().invoke(new ButtplugEvent());
        } else {
            Log.d(TAG, "BLE not Scanning");
        }
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

}
