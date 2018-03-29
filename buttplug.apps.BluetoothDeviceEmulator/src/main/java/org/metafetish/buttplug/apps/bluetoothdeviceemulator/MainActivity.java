package org.metafetish.buttplug.apps.bluetoothdeviceemulator;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.metafetish.buttplug.core.ButtplugLogManager;
import org.metafetish.buttplug.core.IButtplugLog;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.devices.FleshlightLaunchBluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.KiirooBluetoothInfo;
import org.metafetish.buttplug.server.bluetooth.devices.VorzeA10CycloneInfo;
import org.metafetish.buttplug.server.util.FleshlightHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ButtplugLogManager bpLogManager = new ButtplugLogManager();
    private IButtplugLog bpLogger = this.bpLogManager.getLogger(this.getClass());

    private List<IBluetoothDeviceInfo> deviceInfos;

    private String device;

    private SharedPreferences sharedPreferences;

    private TextView status;
    private ImageView icon;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothGattCharacteristic tx;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseSettings advertiseSettings;
    private AdvertiseCallback advertiseCallback;
    private AdvertiseData advertiseData;
    private double lastPosition = 10;
    private byte[] lastCommand;
    private Handler handler = new Handler();
    private ObjectAnimator objectAnimator;

    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.deviceInfos = new ArrayList<>();
        deviceInfos.add(new FleshlightLaunchBluetoothInfo());
        deviceInfos.add(new KiirooBluetoothInfo());
        deviceInfos.add(new VorzeA10CycloneInfo());

        List<String> deviceNames = new ArrayList<>();
        deviceNames.add("Fleshlight Launch");
        deviceNames.add("Kiiroo Pearl");
        deviceNames.add("Vorze A10 Cyclone");

        this.sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        this.device = this.sharedPreferences.getString("device", "Fleshlight Launch");

        // Setup spinner
        Spinner spinner = findViewById(R.id.device);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(this.device));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.this.bpLogger.debug(String.format("Tab %s selected", i));
                MainActivity.this.device = (String) adapterView.getItemAtPosition(i);
                SharedPreferences.Editor editor = MainActivity.this.sharedPreferences.edit();
                editor.putString("device", MainActivity.this.device);
                editor.apply();
                prepareDevice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void prepareDevice() {
        this.status = findViewById(R.id.status);
        this.icon = findViewById(R.id.icon);

        if (this.objectAnimator != null) {
            this.objectAnimator.cancel();
        }
        this.icon.setTranslationY(0);
        this.icon.setRotation(0);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context
                .BLUETOOTH_SERVICE);

        if (this.bluetoothGattServer != null) {
            this.bpLogger.trace("closeGattServer()");
            this.bluetoothGattServer.close();
            this.connected = false;
        }
        if (this.bluetoothLeAdvertiser != null) {
            this.bpLogger.trace("stopAdvertising()");
            this.bluetoothLeAdvertiser.stopAdvertising(this.advertiseCallback);
        }
        if (bluetoothManager != null) {
            this.bpLogger.trace("openGattServer()");
            this.bluetoothGattServer = bluetoothManager.openGattServer(this, new
                    BluetoothGattServerCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothDevice device, int status, int
                                newState) {
                            super.onConnectionStateChange(device, status, newState);
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                MainActivity.this.connected = true;
                                MainActivity.this.status.setText(R.string.gatt_connected);
                                MainActivity.this.bpLogger.trace("onConnectionStateChange(): STATE_CONNECTED");
                                if (MainActivity.this.bluetoothLeAdvertiser != null) {
                                    MainActivity.this.bpLogger.trace("stopAdvertising()");
                                    MainActivity.this.bluetoothLeAdvertiser.stopAdvertising(MainActivity
                                            .this.advertiseCallback);
                                }
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                MainActivity.this.connected = false;
                                MainActivity.this.status.setText(R.string.advertising_start);
                                MainActivity.this.bpLogger.trace("onConnectionStateChange(): STATE_DISCONNECTED");
                                MainActivity.this.bpLogger.trace("startAdvertising()");
                                MainActivity.this.bluetoothLeAdvertiser.startAdvertising(
                                        MainActivity.this.advertiseSettings,
                                        MainActivity.this.advertiseData,
                                        MainActivity.this.advertiseCallback);
                            }
                        }

                        @Override
                        public void onCharacteristicWriteRequest(
                                BluetoothDevice device, int requestId,
                                BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                boolean responseNeeded, int offset, byte[] value) {
                            super.onCharacteristicWriteRequest(device, requestId, characteristic,
                                    preparedWrite, responseNeeded, offset, value);
                            if (characteristic == MainActivity.this.tx) {
                                MainActivity.this.bpLogger.trace(String.format(
                                        "onCharacteristicWriteRequest(): %s on %s", Arrays.toString(value),
                                        characteristic.getUuid().toString()));
                                IBluetoothDeviceInfo deviceInfo = MainActivity.this.deviceInfos.get(
                                        ((Spinner) findViewById(R.id.device)).getSelectedItemPosition());

                                if (deviceInfo instanceof FleshlightLaunchBluetoothInfo) {
                                    if (MainActivity.this.lastCommand != null) {
                                        MainActivity.this.lastPosition = MainActivity.this.lastCommand[0];
                                    }
                                }
                                MainActivity.this.lastCommand = value;
                                MainActivity.this.animateIcon();
                            } else {
                                MainActivity.this.bpLogger.trace(String.format(
                                        "Characteristic Written on non-TX: %s",
                                        characteristic.getUuid().toString()));

                            }
                            MainActivity.this.bluetoothGattServer.sendResponse(device, requestId,
                                    BluetoothGatt.GATT_SUCCESS, offset, value);
                        }
                    });
            this.bpLogger.trace(String.format("bluetoothGattServer services size: %s",
                    this.bluetoothGattServer.getServices().size()));

            IBluetoothDeviceInfo deviceInfo = this.deviceInfos.get(
                    ((Spinner) findViewById(R.id.device)).getSelectedItemPosition());

            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            this.bpLogger.trace(String.format("bluetoothAdapter name: %s", bluetoothAdapter.getName()));
            int deviceIndex = 0;
            if (deviceInfo instanceof KiirooBluetoothInfo) {
                deviceIndex = 1;
            }
            this.bpLogger.trace(String.format("Setting name: %s", deviceInfo.getNames().get(deviceIndex)));
            bluetoothAdapter.setName(deviceInfo.getNames().get(deviceIndex));

            this.bpLogger.trace(String.format("Setting service: %s", deviceInfo.getServices().get(0)));
            BluetoothGattService service = new BluetoothGattService(
                    deviceInfo.getServices().get(0),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);
            int txIndex = 0;
            if (deviceInfo instanceof KiirooBluetoothInfo) {
                txIndex = 1;
            }
            this.bpLogger.trace(String.format("Setting tx: %s",
                    deviceInfo.getCharacteristics().get(txIndex)));
            this.tx = new BluetoothGattCharacteristic(
                    deviceInfo.getCharacteristics().get(txIndex),
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
            service.addCharacteristic(this.tx);
            if (!(deviceInfo instanceof VorzeA10CycloneInfo)) {
                this.bpLogger.trace(String.format("Setting rx: %s",
                        deviceInfo.getCharacteristics().get(1 - txIndex)));
                BluetoothGattCharacteristic rx = new BluetoothGattCharacteristic(
                        deviceInfo.getCharacteristics().get(1 - txIndex),
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
                service.addCharacteristic(rx);
                if (deviceInfo instanceof FleshlightLaunchBluetoothInfo) {
                    this.bpLogger.trace(String.format("Setting cmd: %s",
                            deviceInfo.getCharacteristics().get(
                                    FleshlightLaunchBluetoothInfo.Chrs.Cmd.ordinal())));
                    BluetoothGattCharacteristic cmd = new BluetoothGattCharacteristic(
                            deviceInfo.getCharacteristics().get(
                                    FleshlightLaunchBluetoothInfo.Chrs.Cmd.ordinal()),
                            BluetoothGattCharacteristic.PROPERTY_WRITE,
                            BluetoothGattCharacteristic.PERMISSION_WRITE);
                    service.addCharacteristic(cmd);
                }
            }
            this.bluetoothGattServer.addService(service);
            this.bpLogger.trace(String.format("bluetoothGattServer services size: %s",
                    this.bluetoothGattServer.getServices().size()));

            this.bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
            settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
            this.advertiseSettings = settingsBuilder.build();

            AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
            dataBuilder.setIncludeDeviceName(true);
            this.advertiseData = dataBuilder.build();

            if (this.bluetoothLeAdvertiser != null) {
                if (!this.connected) {
                    this.status.setText(R.string.advertising_start);
                }
                this.bpLogger.trace("startAdvertising()");
                this.advertiseCallback = new AdvertiseCallback() {
                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);

                        if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
                            MainActivity.this.bpLogger.trace("onStartFailure(): Already started");
                            if (!MainActivity.this.connected) {
                                MainActivity.this.status.setText(R.string.advertising_success);
                            }
                        } else {
                            if (!MainActivity.this.connected) {
                                MainActivity.this.status.setText(R.string.advertising_failure);
                            }
                            MainActivity.this.bluetoothLeAdvertiser.stopAdvertising(this);
                        }
                    }

                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        MainActivity.this.bpLogger.trace("onStartSuccess()");
                        if (!MainActivity.this.connected) {
                            MainActivity.this.status.setText(R.string.advertising_success);
                        } else {
                            MainActivity.this.bluetoothLeAdvertiser.stopAdvertising(this);
                        }
                    }
                };
                this.bluetoothLeAdvertiser.startAdvertising(
                        this.advertiseSettings,
                        this.advertiseData,
                        this.advertiseCallback
                );
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.bpLogger.trace("onDestroy()");
        this.bluetoothLeAdvertiser.stopAdvertising(MainActivity.this.advertiseCallback);
        this.bluetoothGattServer.close();
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

    private void animateIcon() {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                IBluetoothDeviceInfo deviceInfo = MainActivity.this.deviceInfos.get(
                        ((Spinner) findViewById(R.id.device)).getSelectedItemPosition());

                if (MainActivity.this.objectAnimator != null) {
                    MainActivity.this.objectAnimator.cancel();
                }
                if (deviceInfo instanceof FleshlightLaunchBluetoothInfo) {
                    MainActivity.this.icon.setRotation(0);
                    double newPosition = MainActivity.this.lastCommand[0];
                    double speed = MainActivity.this.lastCommand[1];
                    long duration = (long) FleshlightHelper.getDuration(Math.abs(newPosition / 100 -
                            MainActivity.this.lastPosition / 100), speed / 100);

                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                            MainActivity.this.icon, "translationY",
                            MainActivity.this.icon.getTranslationY(),
                            MainActivity.this.fromDp(newPosition));
                    objectAnimator.setDuration(duration);
                    objectAnimator.setInterpolator(new LinearInterpolator());
                    objectAnimator.start();
                } else if (deviceInfo instanceof KiirooBluetoothInfo) {
                    MainActivity.this.icon.setTranslationY(0);
                    MainActivity.this.icon.setRotation(0);
                    float degrees = MainActivity.this.lastCommand[0] - 48;
                    if (degrees > 0) {
                        RotateAnimation rotateAnimation = new RotateAnimation(-degrees, degrees,
                                Animation.RELATIVE_TO_SELF,
                                0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f);
                        rotateAnimation.setDuration(10);
                        rotateAnimation.setRepeatCount(Animation.INFINITE);
                        rotateAnimation.setRepeatMode(Animation.REVERSE);
                        MainActivity.this.icon.startAnimation(rotateAnimation);
                    } else {
                        MainActivity.this.icon.clearAnimation();
                    }
                } else if (deviceInfo instanceof VorzeA10CycloneInfo) {
                    MainActivity.this.icon.setTranslationY(0);
                    float speed = MainActivity.this.lastCommand[2];
                    if (speed != 0) {
                        float degrees;
                        if (speed < 0) {
                            speed += 128;
                            degrees = -360;
                        } else {
                            degrees = 360;
                        }
                        MainActivity.this.objectAnimator = ObjectAnimator.ofFloat(
                                MainActivity.this.icon, "rotation",
                                MainActivity.this.icon.getRotation(),
                                MainActivity.this.icon.getRotation() + degrees);
                        MainActivity.this.objectAnimator.setDuration((long) (50000 / speed));
                        MainActivity.this.objectAnimator.setInterpolator(new LinearInterpolator());
                        MainActivity.this.objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                        MainActivity.this.objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
                        MainActivity.this.objectAnimator.start();
                    }
                }
            }
        });
    }

    private float fromDp(double dp) {
        return (float) (dp * this.getResources().getDisplayMetrics().density);
    }
}
