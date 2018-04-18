package org.metafetish.buttplug.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

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

    public void invoke(final ButtplugEvent event) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                for (IButtplugCallback callback : ButtplugEventHandler.this.callbacks) {
                    callback.invoke(event);
                }
            }
        });
    }
}
