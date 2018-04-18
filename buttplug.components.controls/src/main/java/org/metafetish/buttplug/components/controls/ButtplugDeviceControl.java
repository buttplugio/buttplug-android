package org.metafetish.buttplug.components.controls;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.Messages.DeviceAdded;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.DeviceRemoved;


public class ButtplugDeviceControl extends Fragment {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    @SuppressWarnings("unused")
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass().getSimpleName());

    private IButtplugDeviceApplication application;
    private AppCompatActivity activity;
    private View view;
    private AppBarLayout appBarLayout;
    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView devices;
    private RecyclerView.Adapter devicesAdapter;
    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView.LayoutManager devicesLayoutManager;

    private boolean scanning = false;

    public ButtplugDeviceControl() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.activity != null) {
            this.application = (IButtplugDeviceApplication) this.activity.getApplication();
            this.application.getDeviceAdded().addCallback(this.deviceAddedCallback);
            this.application.getDeviceRemoved().addCallback(this.deviceRemovedCallback);
            this.application.getDevicesReset().addCallback(this.devicesResetCallback);
        }
        this.devicesAdapter = new DevicesAdapter(new Handler());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buttplug_device_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        if (this.activity != null) {
            this.devices = this.activity.findViewById(R.id.devices);
            this.devices.setNestedScrollingEnabled(false);
            this.devices.setHasFixedSize(true);
            this.devicesLayoutManager = new LinearLayoutManager(this.activity);
            this.devices.setLayoutManager(this.devicesLayoutManager);
            this.devices.setAdapter(this.devicesAdapter);

            this.appBarLayout = this.activity.findViewById(R.id.appbar);

            Button scanningToggle = this.activity.findViewById(R.id.scanning_toggle);
            scanningToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((Button) view).getText().toString().equals(
                            ButtplugDeviceControl.this.getString(R.string.start_scanning))) {
                        ButtplugDeviceControl.this.scanning = true;
                        ButtplugDeviceControl.this.application.getStartScanning().invoke(new ButtplugEvent());
                        ButtplugDeviceControl.this.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ButtplugDeviceControl.this.onStartScanning();
                            }
                        });
                    } else if (((Button) view).getText().toString().equals(
                            ButtplugDeviceControl.this.getString(R.string.stop_scanning))) {
                        ButtplugDeviceControl.this.scanning = false;
                        ButtplugDeviceControl.this.application.getStopScanning().invoke(new ButtplugEvent());
                        ButtplugDeviceControl.this.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ButtplugDeviceControl.this.onStopScanning();
                            }
                        });
                    }
                }
            });

            if (this.scanning) {
                this.onStartScanning();
            }
        }
    }

    private IButtplugCallback deviceAddedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ((DevicesAdapter) ButtplugDeviceControl.this.devicesAdapter).add(new DeviceMessageInfo((DeviceAdded) event.getMessage()));
        }
    };

    private IButtplugCallback deviceRemovedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ((DevicesAdapter) ButtplugDeviceControl.this.devicesAdapter).remove(((DeviceRemoved) event.getMessage()).deviceIndex);
        }
    };

    private IButtplugCallback devicesResetCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ((DevicesAdapter) ButtplugDeviceControl.this.devicesAdapter).clear();
            ButtplugDeviceControl.this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ButtplugDeviceControl.this.onStopScanning();
                }
            });
        }
    };

    private void onStartScanning() {
        if (this.isUiReady()) {
            ((Button) this.activity.findViewById(R.id.scanning_toggle)).setText(R.string.stop_scanning);
        }
    }

    private void onStopScanning() {
        if (this.isUiReady()) {
            ((Button) this.activity.findViewById(R.id.scanning_toggle)).setText(R.string.start_scanning);
        }
    }

    public boolean isUiReady() {
        return this.activity != null && this.view != null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.view = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (AppCompatActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this.appBarLayout != null) {
            if (isVisibleToUser) {
                this.appBarLayout.setExpanded(false);
            }
        }
    }
}
