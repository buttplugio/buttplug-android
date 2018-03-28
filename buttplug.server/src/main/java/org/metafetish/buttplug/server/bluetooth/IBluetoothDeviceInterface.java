package org.metafetish.buttplug.server.bluetooth;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugMessage;

import java.util.UUID;
import java.util.concurrent.Future;

public interface IBluetoothDeviceInterface {
    String getName();

    Future<ButtplugMessage> writeValue(long msgId, UUID characteristicIndex, byte[]
            value);

    Future<ButtplugMessage> writeValue(long msgId, UUID characteristicIndex, byte[]
            value, boolean writeWithResponse);

    String getAddress();

    @NonNull
    ButtplugEventHandler getDeviceRemoved();

    void disconnect();
}
