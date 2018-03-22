package org.metafetish.buttplug.apps.exampleclientgui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import org.metafetish.buttplug.components.controls.ButtplugTabControl;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.Events.CreateView;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugLog;

public class MainActivity extends AppCompatActivity {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

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

    private boolean backPressed = false;

    @Override
    public void onBackPressed() {
        if (this.backPressed) {
            super.onBackPressed();
            this.finish();
        } else {
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();
            this.backPressed = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.backPressed = false;
                }
            }, 2000);
        }
    }
}
