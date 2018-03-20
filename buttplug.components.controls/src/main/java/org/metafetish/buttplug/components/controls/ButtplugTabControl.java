package org.metafetish.buttplug.components.controls;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;

import org.metafetish.buttplug.core.ButtplugEventHandler;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.Events.CreateView;
import org.metafetish.buttplug.server.ButtplugServer;
import org.metafetish.buttplug.server.DeviceManager;
import org.metafetish.buttplug.server.IButtplugServerFactory;
import org.metafetish.buttplug.server.managers.androidbluetoothmanager.AndroidBluetoothManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ButtplugTabControl.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ButtplugTabControl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ButtplugTabControl extends Fragment implements IButtplugServerFactory {
    private static final String TAG = ButtplugTabControl.class.getSimpleName();

    private String serverName;
    private long maxPingTime;
    public boolean hasDevicePanel = false;
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
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public SectionsPagerAdapter sectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;
    public TabLayout tabLayout;

    private OnFragmentInteractionListener listener;

    public ButtplugTabControl() {
        //TODO: Implement _guiLog
        //TODO: Implement AboutControl
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ButtplugTabControl.
     */
    // TODO: Rename and change types and number of parameters
    public static ButtplugTabControl newInstance() {
        return new ButtplugTabControl();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
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

            this.creatingView.invoke(new CreateView(this));
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (this.listener != null) {
            this.listener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.listener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    private ButtplugServer initializeButtplugServer(String serverName, long maxPingTime) {
        // Set up internal services
        ButtplugServer bpServer;

        //TODO: Is this the case with Android?
        // Due to the weird inability to close BLE devices, we have to share device managers
        // across buttplug
        // server instances. Otherwise we'll just hold device connections open forever.
        if (this.deviceManager == null) {
            bpServer = new ButtplugServer(serverName, maxPingTime);
            this.deviceManager = bpServer.getDeviceManager();
        } else {
            bpServer = new ButtplugServer(serverName, maxPingTime, this.deviceManager);
            return bpServer;
        }

        bpServer.addDeviceSubtypeManager(new AndroidBluetoothManager(getActivity(), new
                ButtplugLogManager()));

        return bpServer;
    }

    public void setServerDetails(String serverName, long maxPingTime) {
        this.serverName = serverName;
        this.maxPingTime = maxPingTime;
    }

    @Override
    public ButtplugServer getServer() {
//        if (this.serverName == null) {
//            throw new Exception("setServerDetails() must be called before getServer()");
//        }
        return this.initializeButtplugServer(this.serverName, this.maxPingTime);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();

            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout
                    .fragment_buttplug_tab_control_placeholder_fragment, container, false);

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                TabLayout tabLayout = (TabLayout) activity.findViewById(R.id.tabs);
//                    Log.d(TAG, "Tab " + getArguments().getInt(ARG_SECTION_NUMBER));
                int tabIndex = getArguments().getInt(ARG_SECTION_NUMBER);
                if (tabIndex >= 0 && tabIndex <= tabLayout.getTabCount() - 1) {
                    String tabText = tabLayout.getTabAt(tabIndex).getText().toString();

                    TextView textView = (TextView) rootView.findViewById(R.id.section_label);
                    textView.setText(getString(R.string.section_format, tabText));
                }
            }
            return rootView;
        }
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
            if (position == this.count - 1) {
                return new ButtplugAboutControl();
            } else {
                Fragment fragment = this.instances.get(position);
                if (fragment == null) {
                    return PlaceholderFragment.newInstance(position);
                } else {
                    return fragment;
                }
            }
        }

        @Override
        public int getCount() {
            return this.count;
        }
    }
}
