package org.metafetish.buttplug.core.Events;

import android.support.v4.app.Fragment;

import org.metafetish.buttplug.core.ButtplugEvent;


public class CreateView extends ButtplugEvent {
    public Fragment fragment;

    public CreateView(Fragment fragment) {
        this.fragment = fragment;
    }
}
