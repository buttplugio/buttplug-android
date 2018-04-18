package org.metafetish.buttplug.server.managers.androidbluetoothmanager;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugDevice;
import org.metafetish.buttplug.server.bluetooth.BluetoothSubtypeManager;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AndroidBluetoothManager extends BluetoothSubtypeManager {
    private static final int REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean scanning;

    private BluetoothAdapter bluetoothAdapter;

    @NonNull
    private List<AndroidBluetoothDeviceFactory> deviceFactories = new ArrayList<>();

    @NonNull
    private List<String> currentlyConnecting;

    public AndroidBluetoothManager(Context context) {
        this.bpLogger.trace("Loading Android Bluetooth Manager");
        this.currentlyConnecting = new ArrayList<>();


        // Introspect the ButtplugDevices namespace for all Factory classes, then create
        // instances of all of them.
        for (IBluetoothDeviceInfo deviceFactory : this.builtinDevices) {
            this.bpLogger.trace(String.format("Loading Bluetooth Device Factory: %s",
                    deviceFactory.getClass().getSimpleName()));
            AndroidBluetoothDeviceFactory androidDeviceFactory = new AndroidBluetoothDeviceFactory(
                    context, deviceFactory);
            androidDeviceFactory.getDeviceCreated().addCallback(new IButtplugCallback() {
                @Override
                public void invoke(ButtplugEvent event) {
                    IButtplugDevice device = event.getDevice();
                    if (device != null) {
                        AndroidBluetoothManager.this.bpLogger.trace(String.format("Device created (%s)",
                                device.getIdentifier()));
                        AndroidBluetoothManager.this.getDeviceAdded().invoke(new ButtplugEvent
                                (device));
                        AndroidBluetoothManager.this.currentlyConnecting.remove(device
                                .getIdentifier());
                    } else {
                        AndroidBluetoothManager.this.bpLogger.trace(String.format("Failed to create device (%s)",
                                event.getString()));
                        AndroidBluetoothManager.this.currentlyConnecting.remove(event.getString());
                    }
                }
            });
            this.deviceFactories.add(androidDeviceFactory);
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.bpLogger.trace("BLE not supported.");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(((Activity) context), new String[]{Manifest.permission
                    .ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
        }

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context
                .BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            this.bpLogger.trace("bluetoothManager is null.");
            return;
        }

        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) {
            this.bpLogger.trace("bluetoothAdapter is null.");
            return;
        }
        if (!this.bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
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

            AndroidBluetoothManager.this.bpLogger.trace(String.format("BLE device found: %s", advertName));

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
                AndroidBluetoothManager.this.bpLogger.trace(String.format("No UUIDs found: %s", advertName));
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
                    AndroidBluetoothManager.this.bpLogger.trace(
                            String.format("Found multiple BLE factories for: %s", advertName));
                } else {
                    AndroidBluetoothManager.this.bpLogger.trace(
                            String.format("No BLE factories found for device: %s", advertName));
                }
                return;
            }

            AndroidBluetoothManager.this.currentlyConnecting.add(btAddr);

            AndroidBluetoothDeviceFactory factory = factories.get(0);
            AndroidBluetoothManager.this.bpLogger.trace(String.format("Found BLE factory: %s",
                    factory.getClass().getSimpleName()));

            // If we actually have a factory for this device, go ahead and create the device
            factory.createDeviceAsync(device);
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    public void startScanning() {
        this.bpLogger.trace("Starting BLE Scanning");
        if (!this.scanning) {
            this.scanning = true;
            this.bluetoothAdapter.startLeScan(this.leScanCallback);
            this.bpLogger.trace("Started BLE Scanning");
        } else {
            this.bpLogger.trace("BLE already Scanning");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void stopScanning() {
        this.bpLogger.trace("Stopping BLE Scanning");
        if (this.scanning) {
            this.scanning = false;
            this.bluetoothAdapter.stopLeScan(this.leScanCallback);
            this.bpLogger.trace("Stopped BLE Scanning");
            this.getScanningFinished().invoke(new ButtplugEvent());
        } else {
            this.bpLogger.trace("BLE not Scanning");
        }
    }

    @Override
    public boolean isScanning() {
        return this.scanning;
    }

}
