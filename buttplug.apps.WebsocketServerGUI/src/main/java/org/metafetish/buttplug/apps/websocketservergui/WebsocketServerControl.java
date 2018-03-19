package org.metafetish.buttplug.apps.websocketservergui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.metafetish.buttplug.components.controls.ButtplugTabControl;
import org.metafetish.buttplug.components.websocketserver.ButtplugWebsocketServer;
import org.metafetish.buttplug.core.ButtplugEvent;
import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.Events.Connection;
import org.metafetish.buttplug.core.IButtplugCallback;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.server.IButtplugServerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WebsocketServerControl.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WebsocketServerControl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebsocketServerControl extends Fragment {
    private static final String TAG = WebsocketServerControl.class.getSimpleName();

    private AppCompatActivity activity;
    private ButtplugWebsocketServer ws;
    private IButtplugServerFactory bpFactory;
    //TODO: Implement ButtplugConfig?
    private ButtplugLogManager bpLogManager;
    private IButtplugLog bpLogger;
    private boolean loopback;
    private long port;
    private boolean secure;
    private SharedPreferences sharedPreferences;
//    private ConnUrlList _connUrls;
//    private Timer _toastTimer;
//    private string _currentExceptionMessage;

    private String remoteId;

    private OnFragmentInteractionListener listener;

    public WebsocketServerControl() {
        // Required empty public constructor
    }

    //TODO: Switch to getParentFragment()
    @SuppressLint("ValidFragment")
    public WebsocketServerControl(ButtplugTabControl bpTabControl) {
        this.bpFactory = bpTabControl;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WebsocketServerControl.
     */
    public static WebsocketServerControl newInstance() {
        return new WebsocketServerControl();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_websocket_server_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            this.activity = activity;
            this.sharedPreferences = this.activity.getPreferences(Context.MODE_PRIVATE);
            this.bpLogManager = new ButtplugLogManager();
            this.bpLogger = this.bpLogManager.getLogger(this.getClass());
            this.ws = new ButtplugWebsocketServer(activity);

            //TODO: Why doesn't this work?
            //this.bpFactory = (ButtplugTabControl) getParentFragment();

//              this._config = new ButtplugConfig("Buttplug");
//              this._connUrls = new ConnUrlList();

            this.port = this.sharedPreferences.getLong("port", 12345);
            this.loopback = this.sharedPreferences.getBoolean("loopback", false);
            this.secure = this.sharedPreferences.getBoolean("secure", false);

            EditText portText = (EditText) this.activity.findViewById(R.id.port);
            portText.setText(String.valueOf(this.port));
            portText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    WebsocketServerControl.this.port = Long.valueOf(charSequence.toString());
                    SharedPreferences.Editor editor = WebsocketServerControl.this
                            .sharedPreferences.edit();
                    editor.putLong("port", WebsocketServerControl.this.port);
                    editor.apply();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            Switch loopbackSwitch = (Switch) this.activity.findViewById(R.id.loopback);
            loopbackSwitch.setChecked(this.loopback);
            loopbackSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    WebsocketServerControl.this.loopback = isChecked;
                    SharedPreferences.Editor editor = WebsocketServerControl.this
                            .sharedPreferences.edit();
                    editor.putBoolean("loopback", WebsocketServerControl.this.loopback);
                    editor.apply();
                }
            });

            Switch secureSwitch = (Switch) this.activity.findViewById(R.id.secure);
            secureSwitch.setChecked(this.secure);
            secureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    WebsocketServerControl.this.secure = isChecked;
                    SharedPreferences.Editor editor = WebsocketServerControl.this
                            .sharedPreferences.edit();
                    editor.putBoolean("secure", WebsocketServerControl.this.secure);
                    editor.apply();
                }
            });

            Button serverToggle = (Button) this.activity.findViewById(R.id.server_toggle);
            serverToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((Button) view).getText().toString().equals(getString(R.string
                            .server_start))) {
                        ((TextView) WebsocketServerControl.this.activity.findViewById(R.id
                                .last_error)).setText("");
                        WebsocketServerControl.this.startServer();
                    } else if (((Button) view).getText().toString().equals(getString(R.string
                            .server_stop))) {
                        WebsocketServerControl.this.stopServer();
                    }
                }
            });

            Button clientToggle = (Button) this.activity.findViewById(R.id.client_toggle);
            clientToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Clicked disconnect");
                    WebsocketServerControl.this.ws.disconnect();
                }
            });

            this.ws.getOnException().addCallback(this.websocketException);
            this.ws.getConnectionAccepted().addCallback(this.websocketConnectionAccepted);
            this.ws.getConnectionUpdated().addCallback(this.websocketConnectionAccepted);
            this.ws.getConnectionClosed().addCallback(this.websocketConnectionClosed);
            this.startServer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.ws.disconnect();
        this.ws.stopServer();
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

    private IButtplugCallback websocketException = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
//            _toastTimer.Enabled = true;
            Exception exception = event.getException();
            String errorMessage = exception.getMessage();

            Log.d(TAG, errorMessage);
            if (WebsocketServerControl.this.secure && errorMessage.contains("Not GET request") &&
                    WebsocketServerControl.this.ws != null) {  // && !ex.IsTerminating)
                WebsocketServerControl.this.bpLogger.logException(exception, true, errorMessage);
                return;
            }

            if (WebsocketServerControl.this.secure) {
                if (errorMessage.contains("The handshake failed due to an unexpected packet " +
                        "format")) {
                    errorMessage += "\n\nThis usually means that the client/browser tried to " +
                            "connect without SSL. Make sure the client is set use the wss:// URI " +
                            "scheme.";
                }
                //TODO: When should this be used?
//                else {
//                    errorMessage += "\n\nIf your connection is working, you can ignore this " +
//                            "message. Otherwise, this could mean that the client/browser has
// not " +
//                            "accepted our SSL certificate. Try hitting the test button on the " +
//                            "\"Websocket Server\" tab.";
//                }
            }

            ((TextView) WebsocketServerControl.this.activity.findViewById(R.id.last_error))
                    .setText(errorMessage);

            WebsocketServerControl.this.bpLogger.logException(exception, true, errorMessage);
            //TODO: Implement isTerminating
            WebsocketServerControl.this.ws.stopServer();
        }
    };
    private IButtplugCallback websocketConnectionAccepted = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            if (event instanceof Connection) {
                WebsocketServerControl.this.remoteId = ((Connection) event).clientName;
            } else {
                WebsocketServerControl.this.remoteId = event.getString();
            }
            if (WebsocketServerControl.this.activity != null) {
                WebsocketServerControl.this.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) WebsocketServerControl.this.activity.findViewById(R.id
                                .status)).setText(getString(R.string.status_connected,
                                WebsocketServerControl.this.remoteId));
                        WebsocketServerControl.this.activity.findViewById(R.id.client_toggle)
                                .setEnabled(true);
                    }
                });
            }
        }
    };

    private IButtplugCallback websocketConnectionClosed = new IButtplugCallback() {
        @Override
        public void invoke(ButtplugEvent event) {
            if (WebsocketServerControl.this.activity != null) {
                WebsocketServerControl.this.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) WebsocketServerControl.this.activity.findViewById(R.id
                                .status)).setText(R
                                .string.status_not_connected);
                        WebsocketServerControl.this.activity.findViewById(R.id.client_toggle)
                                .setEnabled(false);
                    }
                });
            }
        }
    };

    @SuppressLint("StaticFieldLeak")
    public void startServer() {
        try {
            Map<String, String> hostPairs = this.ws.getHostPairs(this.loopback);
            this.ws.startServer(this.bpFactory, this.loopback, (int) this.port, this.secure ?
                    hostPairs : null);
            ((Button) this.activity.findViewById(R.id.server_toggle)).setText(R.string.server_stop);
            this.activity.findViewById(R.id.port).setEnabled(false);
            this.activity.findViewById(R.id.loopback).setEnabled(false);
            this.activity.findViewById(R.id.secure).setEnabled(false);
            ((TextView) this.activity.findViewById(R.id.addresses)).setText(TextUtils.join("\n",
                    hostPairs.keySet()));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        this.ws.stopServer();
        ((Button) this.activity.findViewById(R.id.server_toggle)).setText(R.string.server_start);
        ((EditText) this.activity.findViewById(R.id.port)).setEnabled(true);
        ((Switch) this.activity.findViewById(R.id.loopback)).setEnabled(true);
        ((Switch) this.activity.findViewById(R.id.secure)).setEnabled(true);
        ((TextView) this.activity.findViewById(R.id.addresses)).setText("");
    }
}
