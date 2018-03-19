package org.metafetish.buttplug.apps.exampleclientgui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.metafetish.buttplug.components.controls.ButtplugTabControl;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.Events.CreateView;
import org.metafetish.buttplug.core.IButtplugCallback;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ButtplugTabControl tabs = new ButtplugTabControl();
        tabs.hasDevicePanel = true;
        tabs.setServerDetails("Websocket Server", 1000);
        tabs.getCreatingView().addCallback(new IButtplugCallback() {
            @Override
            public void invoke(ButtplugEvent event) {
                ButtplugTabControl tabControl = (ButtplugTabControl) ((CreateView) event).fragment;
                tabControl.sectionsPagerAdapter.instances.put(0, new ExampleClientPanel
                        (tabControl));
                tabControl.tabLayout.getTabAt(0).setText(R.string.app_short_name);
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.main_content, tabs).commit();
    }
}
