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

    @NonNull
    public ButtplugEventHandler deviceConnected = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler deviceRemoved = new ButtplugEventHandler();

    AndroidBluetoothDeviceInterface(@NonNull Activity aActivity,
                                    @NonNull IButtplugLogManager aLogManager,
                                    @NonNull BluetoothDevice aDevice,
                                    @NonNull IBluetoothDeviceInfo deviceInfo) {
        this.activity = aActivity;
        this.bpLogger = aLogManager.getLogger(this.getClass());

        BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
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
        bleDevice = aDevice.connectGatt(activity, false, mGattCallback);
    }

    BluetoothDevice getDevice() {
        return bleDevice.getDevice();
    }

    List<BluetoothGattService> getServices() {
        return bleDevice.getServices();
    }

    void setGattCharacteristics(@NonNull Map<UUID, BluetoothGattCharacteristic> aCharacteristics) {
        gattCharacteristics = aCharacteristics;
    }

    public String getAddress() throws NullPointerException {
        return bleDevice.getDevice().getAddress();
    }

    public ListenableFuture<ButtplugMessage> writeValue(long aMsgId, UUID aCharacteristicIndex,
                                                        byte[] aValue) {
        return writeValue(aMsgId, aCharacteristicIndex, aValue, false);
    }

    public ListenableFuture<ButtplugMessage> writeValue(long aMsgId, UUID aCharacteristicIndex,
                                                        byte[] aValue, boolean aWriteWithResponse) {
        SettableListenableFuture<ButtplugMessage> promise = new SettableListenableFuture<>();
        if (isCommunicating != null && isCommunicating) {
            Log.d(TAG, "Device transfer in progress, cancelling new transfer.");
        }

        BluetoothGattCharacteristic gattCharacteristic = gattCharacteristics.get
                (aCharacteristicIndex);
        if (gattCharacteristic == null) {
            promise.set(new Error("Requested characteristic " + aCharacteristicIndex.toString() +
                    " not found", Error.ErrorClass.ERROR_DEVICE, aMsgId));
            return promise;
        }
        isCommunicating = true;
        gattCharacteristic.setValue(aValue);
        boolean success = bleDevice.writeCharacteristic(gattCharacteristic);
        if (!success) {
            promise.set(new Error("Failed to write to characteristic " + aCharacteristicIndex
                    .toString(), Error.ErrorClass.ERROR_DEVICE, aMsgId));
            return promise;
        }
        promise.set(new Ok(aMsgId));
        return promise;
    }

    public void disconnect() {
        deviceRemoved.invoke(new ButtplugEvent(bleDevice.getDevice().getAddress()));
        gattCharacteristics.clear();
        bleDevice.disconnect();
    }
}
