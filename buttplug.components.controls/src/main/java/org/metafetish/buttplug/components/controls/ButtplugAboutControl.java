package org.metafetish.buttplug.components.controls;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.IButtplugLog;


/**
 * A simple {@link Fragment} subclass.
 */
public class ButtplugAboutControl extends Fragment {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

    private AppCompatActivity activity;


    public ButtplugAboutControl() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buttplug_about_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.activity != null) {

            TextView headerText = (TextView) ButtplugAboutControl.this.activity.findViewById(R.id
                    .header_text);
            try {
                headerText.setText(getString(R.string.header_title, ButtplugAboutControl.this
                        .activity.getPackageManager().getPackageInfo(ButtplugAboutControl.this
                                .activity.getPackageName(), 0).versionName));
            } catch (PackageManager.NameNotFoundException e) {
                headerText.setText(getString(R.string.header_title, ""));
            }

            //TODO: Implement developer tab?

            TextView linkText = (TextView) ButtplugAboutControl.this.activity.findViewById(R.id
                    .header_links);
            linkText.setMovementMethod(LinkMovementMethod.getInstance());

            ImageView patreon = (ImageView) ButtplugAboutControl.this.activity.findViewById(R.id
                    .patreon);
            patreon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("http://patreon.com/qdot"));
                    startActivity(intent);
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

}
