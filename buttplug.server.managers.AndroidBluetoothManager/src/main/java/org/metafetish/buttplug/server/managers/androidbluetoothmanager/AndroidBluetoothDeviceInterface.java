package org.metafetish.buttplug.server.managers.androidbluetoothmanager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AndroidBluetoothDeviceInterface implements IBluetoothDeviceInterface {
    private static final String TAG = AndroidBluetoothDeviceInterface.class.getSimpleName();
    private Activity activity;

    public String getName() {
        return bleDevice.getDevice().getName();
    }

    @NonNull
    private Map<UUID, BluetoothGattCharacteristic> gattCharacteristics = new HashMap<>();

    @NonNull
    private IButtplugLog bpLogger;

    @NonNull
    private BluetoothGatt bleDevice;

    @Nullable
    private Boolean isCommunicating;

    private ButtplugEventHandler deviceConnected = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getDeviceConnected() {
        return this.deviceConnected;
    }

    private ButtplugEventHandler deviceRemoved = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getDeviceRemoved() {
        return this.deviceRemoved;
    }

    AndroidBluetoothDeviceInterface(@NonNull Activity activity,
                                    @NonNull IButtplugLogManager logManager,
                                    @NonNull BluetoothDevice device,
                                    @NonNull IBluetoothDeviceInfo deviceInfo) {
        this.activity = activity;
        this.bpLogger = logManager.getLogger(this.getClass());

        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d(TAG, "New State: " + newState);
//                if ()
                if (newState == BluetoothAdapter.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
                    deviceRemoved.invoke(new ButtplugEvent(gatt.getDevice().getAddress()));
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    deviceConnected.invoke(new ButtplugEvent(gatt.getDevice().getAddress()));
                } else {
                    gatt.disconnect();
                }
            }
        };
        bleDevice = device.connectGatt(activity, false, gattCallback);
    }

    BluetoothDevice getDevice() {
        return bleDevice.getDevice();
    }

    List<BluetoothGattService> getServices() {
        return bleDevice.getServices();
    }

    void setGattCharacteristics(@NonNull Map<UUID, BluetoothGattCharacteristic> characteristics) {
        gattCharacteristics = characteristics;
    }

    public String getAddress() throws NullPointerException {
        return bleDevice.getDevice().getAddress();
    }

    public ListenableFuture<ButtplugMessage> writeValue(long msgId, UUID characteristicIndex,
                                                        byte[] value) {
        return writeValue(msgId, characteristicIndex, value, false);
    }

    public ListenableFuture<ButtplugMessage> writeValue(long msgId, UUID characteristicIndex,
                                                        byte[] value, boolean writeWithResponse) {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();
        if (isCommunicating != null && isCommunicating) {
            Log.d(TAG, "Device transfer in progress, cancelling new transfer.");
        }

        BluetoothGattCharacteristic gattCharacteristic = gattCharacteristics.get
                (characteristicIndex);
        if (gattCharacteristic == null) {
            promise.set(new Error("Requested characteristic " + characteristicIndex.toString() +
                    " not found", Error.ErrorClass.ERROR_DEVICE, msgId));
            return promise;
        }
        isCommunicating = true;
        gattCharacteristic.setValue(value);
        boolean success = bleDevice.writeCharacteristic(gattCharacteristic);
        if (!success) {
            promise.set(new Error("Failed to write to characteristic " + characteristicIndex
                    .toString(), Error.ErrorClass.ERROR_DEVICE, msgId));
            return promise;
        }
        promise.set(new Ok(msgId));
        return promise;
    }

    public void disconnect() {
        deviceRemoved.invoke(new ButtplugEvent(bleDevice.getDevice().getAddress()));
        gattCharacteristics.clear();
        bleDevice.disconnect();
    }
}
