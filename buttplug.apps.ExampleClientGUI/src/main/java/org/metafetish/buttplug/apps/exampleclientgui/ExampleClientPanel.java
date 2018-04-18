package org.metafetish.buttplug.apps.exampleclientgui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.metafetish.buttplug.client.ButtplugWSClient;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.Error;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


public class ExampleClientPanel extends Fragment {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass().getSimpleName());

    public ConcurrentHashMap<Long, DeviceMessageInfo> devices = new ConcurrentHashMap<>();
    private ButtplugWSClient client;
    private String address;

    private ExampleClientDeviceApplication application;
    private AppCompatActivity activity;
    private View view;

    private SharedPreferences sharedPreferences;

    private boolean connecting = false;
    private boolean connected = false;

    private String currentExceptionMessage = "";

    public ExampleClientPanel() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.activity != null) {
            this.application = (ExampleClientDeviceApplication) this.activity.getApplication();
            this.application.getStartScanning().addCallback(this.startScanningCallback);
            this.application.getStopScanning().addCallback(this.stopScanningCallback);
            this.sharedPreferences = this.activity.getPreferences(Context.MODE_PRIVATE);
            this.bpLogger.getOnLogException().addCallback(this.exceptionLogged);
            this.address = this.sharedPreferences.getString("address", this.getString(R.string.default_address));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_example_client_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        if (this.activity != null) {
            EditText portText = this.activity.findViewById(R.id.address);
            portText.setText(this.address);
            portText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ExampleClientPanel.this.address = charSequence.toString();
                    SharedPreferences.Editor editor = ExampleClientPanel.this.sharedPreferences.edit();
                    editor.putString("address", ExampleClientPanel.this.address);
                    editor.apply();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            Button clientToggle = (Button) this.activity.findViewById(R.id.client_toggle);
            clientToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((Button) view).getText().toString().equals(
                            ExampleClientPanel.this.getString(R.string.client_connect))) {
                        ExampleClientPanel.this.clientConnect();
                    } else if (((Button) view).getText().toString().equals(
                            ExampleClientPanel.this.getString(R.string.client_disconnect))) {
                        ExampleClientPanel.this.clientDisconnect();
                    }
                }
            });

            if (this.connected) {
                this.onClientConnect();
            }
            if (!this.currentExceptionMessage.isEmpty()) {
                this.onError();
            }
        }
    }

    private IButtplugCallback startScanningCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            try {
                ExampleClientPanel.this.client.startScanning();
            } catch (ExecutionException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    };

    private IButtplugCallback stopScanningCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            try {
                ExampleClientPanel.this.client.stopScanning();
            } catch (ExecutionException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    };

    private IButtplugCallback deviceAdded = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ExampleClientPanel.this.application.getDeviceAdded().invoke(event);
        }
    };

    private IButtplugCallback deviceRemoved = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ExampleClientPanel.this.application.getDeviceAdded().invoke(event);
        }
    };

    private IButtplugCallback exceptionLogged = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ExampleClientPanel.this.currentExceptionMessage = ((Error) event.getMessage()).errorMessage;
            ExampleClientPanel.this.clientDisconnect();
            ExampleClientPanel.this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ExampleClientPanel.this.onError();
                }
            });
        }
    };

    private IButtplugCallback connectedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ExampleClientPanel.this.connected = true;
            ExampleClientPanel.this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ExampleClientPanel.this.onClientConnected();
                }
            });
        }
    };

    private IButtplugCallback disconnectedCallback = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            ExampleClientPanel.this.clientDisconnect();
        }
    };

    private void clientConnect() {
        if (!this.connected) {
            try {
                this.connecting = true;
                this.currentExceptionMessage = "";
                this.client = new ButtplugWSClient("Example Client GUI");
                this.client.deviceAdded.addCallback(this.deviceAdded);
                this.client.deviceRemoved.addCallback(this.deviceRemoved);
                this.client.errorReceived.addCallback(this.exceptionLogged);
                this.client.connected.addCallback(this.connectedCallback);
                this.client.disconnected.addCallback(this.disconnectedCallback);
                this.client.connect(this.address, true);
                ExampleClientPanel.this.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ExampleClientPanel.this.onClientConnect();
                        ExampleClientPanel.this.application.getDevicesReset().invoke(new ButtplugEvent());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clientDisconnect() {
        if (this.connecting ||  this.connected) {
            if (this.connected) {
                this.connected = false;
                this.client.disconnect();
            } else {
                this.connecting = false;
            }
            ExampleClientPanel.this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ExampleClientPanel.this.onClientDisconnect();
                    ExampleClientPanel.this.application.getDevicesReset().invoke(new ButtplugEvent());
                }
            });
        }
    }

    private void onClientConnect() {
        if (this.isUiReady()) {
            ((Button) this.activity.findViewById(R.id.client_toggle)).setEnabled(false);
            this.activity.findViewById(R.id.address).setEnabled(false);
            ((TextView) this.activity.findViewById(R.id.last_error)).setText("");
        }
    }

    private void onClientDisconnect() {
        if (this.isUiReady()) {
            Button clientToggle = this.activity.findViewById(R.id.client_toggle);
            clientToggle.setText(R.string.client_connect);
            clientToggle.setEnabled(true);
            this.activity.findViewById(R.id.address).setEnabled(true);
        }
    }

    private void onClientConnected() {
        if (this.isUiReady()) {
            Button clientToggle = this.activity.findViewById(R.id.client_toggle);
            clientToggle.setText(R.string.client_disconnect);
            clientToggle.setEnabled(true);
        }
    }

    private void onError() {
        if (this.isUiReady()) {
            ((TextView) this.activity.findViewById(R.id.last_error)).setText(this.currentExceptionMessage);
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
    public void onDestroy() {
        super.onDestroy();
        this.client.disconnect();
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
