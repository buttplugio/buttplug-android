package org.metafetish.buttplug.components.controls;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.Events.CreateView;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.server.ButtplugServer;
import org.metafetish.buttplug.server.DeviceManager;
import org.metafetish.buttplug.server.IButtplugServerFactory;
import org.metafetish.buttplug.server.managers.androidbluetoothmanager.AndroidBluetoothManager;


public class ButtplugTabControl extends Fragment implements IButtplugTabControl, IButtplugServerFactory {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass().getSimpleName());

    private AppCompatActivity activity;
    private String serverName;
    private long maxPingTime;
    private boolean hasDevicePanel = false;
    public void enableDeviceTab() {
        this.hasDevicePanel = true;
    }
    public DeviceManager deviceManager;
    private ButtplugEventHandler creatingView = new ButtplugEventHandler();

    public ButtplugEventHandler getCreatingView() {
        return this.creatingView;
    }

    public ButtplugEventHandler selectedDevicesChanged = new ButtplugEventHandler();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory.
     */
    private SectionsPagerAdapter sectionsPagerAdapter;
    public SectionsPagerAdapter getSectionsPagerAdapter() {
        return this.sectionsPagerAdapter;
    }

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public void setApplicationTab(Fragment fragment, String text) {
        this.sectionsPagerAdapter.instances.put(0, fragment);

        TabLayout.Tab applicationTab = this.tabLayout.getTabAt(0);
        if (applicationTab != null) {
            applicationTab.setText(text);
        }
    }

    public ButtplugTabControl() {
        // Required empty public constructor
        //TODO: Implement Sentry?
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.activity != null) {
            try {
                this.bpLogger.info(String.format("Buttplug %s",
                        this.activity.getPackageManager().getPackageInfo(
                                this.activity.getPackageName(), 0).versionName));
            } catch (PackageManager.NameNotFoundException e) {
                this.bpLogger.error("No version info available!");
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style
                .AppTheme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        // Inflate the layout for this fragment
        return localInflater.inflate(R.layout.fragment_buttplug_tab_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.activity != null) {
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            this.sectionsPagerAdapter = new SectionsPagerAdapter(activity
                    .getSupportFragmentManager(), this.hasDevicePanel ? 4 : 3);

            // Set up the ViewPager with the sections adapter.
            this.viewPager = (ViewPager) activity.findViewById(R.id.container);
            this.viewPager.setAdapter(this.sectionsPagerAdapter);

            this.tabLayout = (TabLayout) activity.findViewById(R.id.tabs);
            if (!this.hasDevicePanel) {
                this.tabLayout.removeTabAt(1);
            }

            this.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener
                    (this.tabLayout));
            this.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener
                    (this.viewPager));
            this.viewPager.setOffscreenPageLimit(2);

            this.creatingView.invoke(new CreateView(this));
        }
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
    public void onDestroy() {
        super.onDestroy();
        ButtplugLogManager.lastLogMessagesReceived.clear();
    }

    private ButtplugServer initializeButtplugServer(String serverName, long maxPingTime) {
        // Set up internal services
        final ButtplugServer bpServer;

        //TODO: Is this the case with Android?
        // Due to the weird inability to close BLE devices, we have to share device managers
        // across buttplug
        // server instances. Otherwise we'll just hold device connections open forever.
        if (this.deviceManager == null) {
            bpServer = new ButtplugServer(serverName, maxPingTime);
            this.deviceManager = bpServer.getDeviceManager();
            bpServer.addDeviceSubtypeManager(new AndroidBluetoothManager(getActivity()));
        } else {
            bpServer = new ButtplugServer(serverName, maxPingTime, this.deviceManager);
        }

        if (maxPingTime != 0) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bpServer.setHandler(new Handler());
                }
            });
        }

        return bpServer;
    }

    public void setServerDetails(String serverName, long maxPingTime) {
        this.serverName = serverName;
        this.maxPingTime = maxPingTime;

        String codeName;
        if (Build.VERSION.SDK_INT <= 20) {
            codeName = "Kit Kat";
        } else if(Build.VERSION.SDK_INT <= 22) {
            codeName = "Lollipop";
        } else if(Build.VERSION.SDK_INT == 23) {
            codeName = "Marshmallow";
        } else if(Build.VERSION.SDK_INT <= 25) {
            codeName = "Nougat";
        } else if(Build.VERSION.SDK_INT <= 27) {
            codeName = "Oreo";
        } else {
            codeName = "P";
        }
        this.bpLogger.info(String.format("Android %s.%s (%s, SDK %s)",
                Build.VERSION.RELEASE,
                Build.VERSION.INCREMENTAL,
                codeName,
                Build.VERSION.SDK_INT));
    }

    @Override
    public ButtplugServer getServer() {
//        if (this.serverName == null) {
//            throw new Exception("setServerDetails() must be called before getServer()");
//        }
        return this.initializeButtplugServer(this.serverName, this.maxPingTime);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SparseArray<Fragment> instances = new SparseArray<>();

        private int count;

        public SectionsPagerAdapter(FragmentManager fm, int count) {
            super(fm);
            this.count = count;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == this.count - 2) {
                return new ButtplugLogControl();
            } else if (position == this.count - 1) {
                return new ButtplugAboutControl();
            } else if (ButtplugTabControl.this.hasDevicePanel && position == 1) {
                return new ButtplugDeviceControl();
            } else {
                return this.instances.get(position);
            }
        }

        @Override
        public int getCount() {
            return this.count;
        }
    }
}
