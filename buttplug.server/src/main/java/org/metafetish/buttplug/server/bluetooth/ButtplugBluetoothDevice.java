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
            @NonNull IButtplugLogManager aLogManager,
            @NonNull String aName,
            @NonNull IBluetoothDeviceInterface aInterface,
            @NonNull IBluetoothDeviceInfo aInfo) {
        super(aLogManager, aName, aInterface.getAddress());
        this.iface = aInterface;
        this.info = aInfo;
        this.iface.deviceRemoved.addCallback(this.deviceRemovedCallback);
    }

    @Override
    public void disconnect() {
        this.iface.disconnect();
    }

    private IButtplugCallback deviceRemovedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent aEvent) {
            ButtplugBluetoothDevice.this.invokeDeviceRemoved();
            ButtplugBluetoothDevice.this.iface.deviceRemoved.removeCallback(ButtplugBluetoothDevice.this.deviceRemovedCallback);
        }
    };
}
