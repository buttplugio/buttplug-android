package org.metafetish.buttplug.apps.websocketservergui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.metafetish.buttplug.components.controls.ButtplugTabControl;
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

        ButtplugTabControl tabs = new ButtplugTabControl();
        tabs.setServerDetails("Websocket Server", 1000);
        tabs.getCreatingView().addCallback(new IButtplugCallback() {
            @Override
            public void invoke(ButtplugEvent event) {
                ButtplugTabControl tabControl = (ButtplugTabControl) ((CreateView) event).fragment;
                tabControl.setApplicationTab(new WebsocketServerControl(tabControl),
                        MainActivity.this.getString(R.string.app_short_name));
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.main_content, tabs).commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context
                            .INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
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
