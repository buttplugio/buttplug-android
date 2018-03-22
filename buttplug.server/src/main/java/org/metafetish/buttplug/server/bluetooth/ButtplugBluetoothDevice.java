package org.metafetish.buttplug.server.bluetooth;


import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugDevice;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugLogManager;

public class ButtplugBluetoothDevice extends ButtplugDevice {
    @NonNull
    protected IBluetoothDeviceInterface iface;

    @NonNull
    protected IBluetoothDeviceInfo info;

    protected ButtplugBluetoothDevice(
            @NonNull String name,
            @NonNull IBluetoothDeviceInterface iface,
            @NonNull IBluetoothDeviceInfo info) {
        super(name, iface.getAddress());
        this.iface = iface;
        this.info = info;
        this.iface.getDeviceRemoved().addCallback(this.deviceRemovedCallback);
    }

    @Override
    public void disconnect() {
        this.iface.disconnect();
    }

    private IButtplugCallback deviceRemovedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ButtplugBluetoothDevice.this.invokeDeviceRemoved();
            ButtplugBluetoothDevice.this.iface.getDeviceRemoved().removeCallback
                    (ButtplugBluetoothDevice.this.deviceRemovedCallback);
        }
    };
}
