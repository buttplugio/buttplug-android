package org.metafetish.buttplug.core;

import java.util.ArrayList;
import java.util.List;

public class ButtplugEventHandler {
    private List<IButtplugCallback> callbacks = new ArrayList<>();

    public void addCallback(IButtplugCallback aCallback) {
        if (!this.callbacks.contains(aCallback)) {
            this.callbacks.add(aCallback);
        }
    }

    public void removeCallback(IButtplugCallback aCallback) {
        if (this.callbacks.contains(aCallback)) {
            this.callbacks.remove(aCallback);
        }
    }

    public void invoke(ButtplugEvent aEvent) {
        for (IButtplugCallback callback : this.callbacks) {
            callback.invoke(aEvent);
        }
    }
}
