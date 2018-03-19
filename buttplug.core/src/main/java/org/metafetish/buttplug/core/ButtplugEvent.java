package org.metafetish.buttplug.core;

public class ButtplugEvent {
    //TODO: Migrate these to .Events subclasses
    private ButtplugMessage msg;
    private IButtplugDevice device;
    private String string;
    private Exception exception;

    public ButtplugEvent() {
    }

    public ButtplugEvent(ButtplugMessage msg) {
        this.msg = msg;
    }

    public ButtplugEvent(IButtplugDevice device) {
        this.device = device;
    }

    public ButtplugEvent(String string) {
        this.string = string;
    }

    public ButtplugEvent(Exception exception) {
        this.exception = exception;
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

    public Exception getException() {
        return this.exception;
    }
}
