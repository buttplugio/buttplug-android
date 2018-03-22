package org.metafetish.buttplug.components.controls;


import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugLogLevel;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.Messages.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class ButtplugLogControl extends Fragment {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());
    private ButtplugLogLevel logLevel = ButtplugLogLevel.DEBUG;

    private AppCompatActivity activity;
    private AppBarLayout appBarLayout;
    private RecyclerView log;
    private RecyclerView.Adapter logAdapter;
    private RecyclerView.LayoutManager logLayoutManager;

    private SharedPreferences sharedPreferences;

    public ButtplugLogControl() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.activity != null) {
            this.sharedPreferences = this.activity.getPreferences(Context.MODE_PRIVATE);
            this.logLevel = ButtplugLogLevel.valueOf(this.sharedPreferences.getString("logLevel", this.logLevel.toString()).toUpperCase());
        }
        this.logAdapter = new LogAdapter(new Handler(), ButtplugLogManager.lastLogMessagesReceived, this.logLevel);
        ButtplugLogManager.globalLogMessageReceived.addCallback(new IButtplugCallback() {
            @Override
            public void invoke(ButtplugEvent event) {
                ((LogAdapter) ButtplugLogControl.this.logAdapter).add((Log) event.getMessage());
            }
        });
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
            this.log.setNestedScrollingEnabled(false);
            this.log.setHasFixedSize(true);
            this.logLayoutManager = new LinearLayoutManager(this.activity);
            this.log.setLayoutManager(this.logLayoutManager);
            this.log.setAdapter(this.logAdapter);

            this.appBarLayout = this.activity.findViewById(R.id.appbar);

            Spinner spinner = (Spinner) this.activity.findViewById(R.id.log_level);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.activity, R
                    .array.log_levels, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(adapter.getPosition(this.logLevel.toString()));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String logLevel = (String) adapterView.getItemAtPosition(i);
                    ButtplugLogControl.this.logLevel = ButtplugLogLevel.valueOf(logLevel.toUpperCase());
                    ((LogAdapter) ButtplugLogControl.this.logAdapter).setLogLevel(ButtplugLogControl.this.logLevel);
                    SharedPreferences.Editor editor = ButtplugLogControl.this.sharedPreferences.edit();
                    editor.putString("logLevel", ButtplugLogControl.this.logLevel.toString());
                    editor.apply();

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            Button clearLog = (Button) this.activity.findViewById(R.id.clear_log);
            clearLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((LogAdapter) ButtplugLogControl.this.logAdapter).clear();
                }
            });

            Button saveLog = (Button) this.activity.findViewById(R.id.save_log);
            saveLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ContextCompat.checkSelfPermission(ButtplugLogControl.this.activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ButtplugLogControl.this.activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                    } else {
                        ButtplugLogControl.this.saveLogMessages();
                    }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.activity, "Failed to save file", Toast.LENGTH_SHORT).show();
                this.bpLogger.error("Permission to write to external storage denied by user");
            }
        }
    }

    public void saveLogMessages() {
        List<String> logMessages = ((LogAdapter) this.logAdapter).getLogMessages();
        DownloadManager downloadManager = (DownloadManager) ButtplugLogControl.this.activity.getSystemService(Context.DOWNLOAD_SERVICE);
        String filename = "Buttplug.log";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        try {
            OutputStream out = new FileOutputStream(file);
            String logFile = TextUtils.join("\n", logMessages);
            out.write(logFile.getBytes());
            out.close();
            Toast.makeText(this.activity, "File saved", Toast.LENGTH_SHORT).show();
            if (downloadManager != null) {
                downloadManager.addCompletedDownload(filename, filename, false, "text/plain", file.getPath(), logFile.length(), true);
            }
        } catch (IOException e) {
            Toast.makeText(this.activity, "Failed to save file", Toast.LENGTH_SHORT).show();
            this.bpLogger.logException(e);
        }
    }
}
