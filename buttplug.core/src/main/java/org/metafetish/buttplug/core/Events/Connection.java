package org.metafetish.buttplug.core.Events;

import org.metafetish.buttplug.core.ButtplugEvent;

public class Connection extends ButtplugEvent {
    public String remoteId;
    public String clientName;

    public Connection(String remoteId, String clientName) {
        this.remoteId = remoteId;
        this.clientName = clientName;
    }
}
