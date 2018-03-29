package org.metafetish.buttplug.server.managers.androidbluetoothmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.util.concurrent.SettableFuture;

import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.IButtplugLogManager;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

public class AndroidBluetoothDeviceInterface implements IBluetoothDeviceInterface {

    public String getName() {
        return this.bleDevice.getDevice().getName();
    }

    @NonNull
    private Map<UUID, BluetoothGattCharacteristic> gattCharacteristics = new HashMap<>();

    @NonNull
    private IButtplugLogManager bpLogManager = new ButtplugLogManager();

    @NonNull
    private IButtplugLog bpLogger = bpLogManager.getLogger(this.getClass());

    @NonNull
    private BluetoothGatt bleDevice;

    @Nullable
    private Boolean isCommunicating;

    private ButtplugEventHandler deviceConnected = new ButtplugEventHandler();

    @NonNull
    ButtplugEventHandler getDeviceConnected() {
        return this.deviceConnected;
    }

    private ButtplugEventHandler deviceRemoved = new ButtplugEventHandler();

    @NonNull
    public ButtplugEventHandler getDeviceRemoved() {
        return this.deviceRemoved;
    }

    AndroidBluetoothDeviceInterface(@NonNull Context context,
                                    @NonNull BluetoothDevice device) {

        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                AndroidBluetoothDeviceInterface.this.bpLogger.trace(String.format("New State: %s", newState));
                if (newState == BluetoothAdapter.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
                    AndroidBluetoothDeviceInterface.this.deviceRemoved.invoke(
                            new ButtplugEvent(gatt.getDevice().getAddress()));
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    AndroidBluetoothDeviceInterface.this.deviceConnected.invoke(
                            new ButtplugEvent(gatt.getDevice().getAddress()));
                } else {
                    gatt.disconnect();
                }
            }
        };
        bleDevice = device.connectGatt(context, false, gattCallback);
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

    public Future<ButtplugMessage> writeValue(long msgId, UUID characteristicIndex,
                                              byte[] value) {
        return writeValue(msgId, characteristicIndex, value, false);
    }

    public Future<ButtplugMessage> writeValue(long msgId, UUID characteristicIndex,
                                                        byte[] value, boolean writeWithResponse) {
        SettableFuture<ButtplugMessage> promise = SettableFuture.create();
        if (isCommunicating != null && isCommunicating) {
            this.bpLogger.trace("Device transfer in progress, cancelling new transfer.");
        }

        BluetoothGattCharacteristic gattCharacteristic = gattCharacteristics.get
                (characteristicIndex);
        if (gattCharacteristic == null) {
            promise.set(new Error(String.format("Requested characteristic %s not found",
                    characteristicIndex.toString()), Error.ErrorClass.ERROR_DEVICE, msgId));
            return promise;
        }
        isCommunicating = true;
        this.bpLogger.trace(String.format("Writing %s to %s.", Arrays.toString(value),
                characteristicIndex.toString()));
        gattCharacteristic.setValue(value);
        boolean success = bleDevice.writeCharacteristic(gattCharacteristic);
        if (!success) {
            promise.set(new Error(String.format("Failed to write to characteristic %s",
                    characteristicIndex.toString()), Error.ErrorClass.ERROR_DEVICE, msgId));
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
