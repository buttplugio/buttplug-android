package org.metafetish.buttplug.core;

public class ButtplugEvent {
    private ButtplugMessage msg;
    private IButtplugDevice device;
    private String string;

    public ButtplugEvent() {
    }

    public ButtplugEvent(ButtplugMessage aMsg) {
        this.msg = aMsg;
    }

    public ButtplugEvent(IButtplugDevice aDevice) {
        this.device = aDevice;
    }

    public ButtplugEvent(String aString) {
        this.string = aString;
    }

    public ButtplugMessage getMessage() {
        return this.msg;
    }

    public IButtplugDevice getDevice() {
        return device;
    }

    public String getString() {
        return this.string;
    }
}
