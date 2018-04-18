package org.metafetish.buttplug.apps.exampleclientgui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import org.metafetish.buttplug.components.controls.ButtplugTabControl;
import org.metafetish.buttplug.components.controls.IButtplugTabControl;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.Events.CreateView;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugLog;

public class MainActivity extends AppCompatActivity {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass().getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        IButtplugTabControl tabs = new ButtplugTabControl();
        tabs.enableDeviceTab();
        tabs.getCreatingView().addCallback(new IButtplugCallback() {
            @Override
            public void invoke(ButtplugEvent event) {
                IButtplugTabControl tabControl = (ButtplugTabControl) ((CreateView) event).fragment;
                tabControl.setApplicationTab(new ExampleClientPanel(),
                        MainActivity.this.getString(R.string.app_short_name));
            }
        });
        this.getSupportFragmentManager().beginTransaction().add(R.id.main_content, (Fragment) tabs).commit();
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
