package org.metafetish.buttplug.components.controls;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


public class ButtplugLogControl extends Fragment {
    private static final String TAG = ButtplugLogControl.class.getSimpleName();

    private AppCompatActivity activity;
    private AppBarLayout appBarLayout;
    private TextView log;
    private SharedPreferences sharedPreferences;

    public ButtplugLogControl() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buttplug_log_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.activity != null) {
            this.log = this.activity.findViewById(R.id.log);

            this.appBarLayout = this.activity.findViewById(R.id.appbar);

            Spinner spinner = (Spinner) this.activity.findViewById(R.id.log_level);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.activity, R
                    .array.log_levels, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    adapterView.getItemAtPosition(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this.appBarLayout != null) {
            if (isVisibleToUser) {
                appBarLayout.setExpanded(false);
            }
        }
    }
}
