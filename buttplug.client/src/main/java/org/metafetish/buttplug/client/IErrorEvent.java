package org.metafetish.buttplug.client;

import org.metafetish.buttplug.core.Messages.Error;

public interface IErrorEvent {
    void errorReceived(Error err);
}
