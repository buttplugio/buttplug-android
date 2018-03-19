package org.metafetish.buttplug.core;

import java.util.ArrayList;
import java.util.List;

public class ButtplugEventHandler {
    private List<IButtplugCallback> callbacks = new ArrayList<>();

    public void addCallback(IButtplugCallback callback) {
        if (!this.callbacks.contains(callback)) {
            this.callbacks.add(callback);
        }
    }

    public void removeCallback(IButtplugCallback callback) {
        if (this.callbacks.contains(callback)) {
            this.callbacks.remove(callback);
        }
    }

    public void invoke(ButtplugEvent event) {
        for (IButtplugCallback callback : this.callbacks) {
            callback.invoke(event);
        }
    }
}
