package org.metafetish.buttplug.components.controls;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;

import org.metafetish.buttplug.core.ButtplugEventHandler;

public interface IButtplugTabControl {
    void enableDeviceTab();
    ButtplugEventHandler getCreatingView();
    void setApplicationTab(Fragment fragment, String text);
}
