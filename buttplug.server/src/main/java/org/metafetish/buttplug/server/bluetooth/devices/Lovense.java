package org.metafetish.buttplug.server.bluetooth.devices;

import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugDeviceMessageCallback;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.RotateCmd;
import org.metafetish.buttplug.core.Messages.SingleMotorVibrateCmd;
import org.metafetish.buttplug.core.Messages.StopDeviceCmd;
import org.metafetish.buttplug.core.Messages.VibrateCmd;
import org.metafetish.buttplug.server.bluetooth.ButtplugBluetoothDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;


public class Lovense extends ButtplugBluetoothDevice {
    private static Map<String, String> friendlyNames = new HashMap<String, String>() {{
        put("LVS-A011", "Nora");
        put("LVS-C011", "Nora");
        put("LVS-B011", "Max");
        put("LVS-L009", "Ambi");
        put("LVS-S0?\\d{2}", "Lush");
        put("LVS_Z0?\\d{2}", "Hush");
        put("LVS-Domi\\d{2}", "Domi");
        put("LVS-P\\d{2}", "Edge");
        put("LVS-Edge\\d{2}", "Edge");
    }};
    private static String getFriendlyName(String unfriendlyname) {
        for (Map.Entry<String, String> entry : friendlyNames.entrySet()) {
            if (Pattern.matches(entry.getKey(), unfriendlyname)) {
                return entry.getValue();
            }
        }
        return "Unknown";
    }


    private int vibratorCount = 1;
    private double[] vibratorSpeeds = {0, 0};
    private boolean clockwise = true;
    private double rotateSpeed;

    public Lovense(IBluetoothDeviceInterface iface,
                   IBluetoothDeviceInfo info) {
        super(String.format("Lovense Device %s", Lovense.getFriendlyName(iface.getName())), iface, info);
        if (this.name.equals("Edge")) {
            ++vibratorCount;
        }

        msgFuncs.put(SingleMotorVibrateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleSingleMotorVibrateCmd));
        msgFuncs.put(VibrateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleVibrateCmd, new MessageAttributes(vibratorCount)));
        msgFuncs.put(StopDeviceCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleStopDeviceCmd));

        if (this.name.equals("Nora")) {
            msgFuncs.put(RotateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleRotateCmd, new MessageAttributes(1)));
        }
    }

    private IButtplugDeviceMessageCallback handleStopDeviceCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            Lovense.this.bpLogger.debug(String.format("Stopping Device %s", Lovense.this.getName()));
            if (Lovense.this.name.equals("Nora")) {
                RotateCmd rotateCmd = new RotateCmd(msg.deviceIndex, null, msg.id);
                ArrayList<RotateCmd.RotateSubcommand> rotations = new ArrayList<>();
                rotations.add(rotateCmd.new RotateSubcommand(0, 0, Lovense.this.clockwise));
                rotateCmd.rotations = rotations;
                return Lovense.this.handleRotateCmd.invoke(rotateCmd);
            }
            return Lovense.this.handleSingleMotorVibrateCmd.invoke(new SingleMotorVibrateCmd(msg.deviceIndex, 0, msg.id));
        }
    };

    private IButtplugDeviceMessageCallback handleSingleMotorVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof SingleMotorVibrateCmd)) {
                return Lovense.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");
            }
            SingleMotorVibrateCmd cmdMsg = (SingleMotorVibrateCmd) msg;

            VibrateCmd vibrateCmd = new VibrateCmd(cmdMsg.deviceIndex, null, cmdMsg.id);
            ArrayList<VibrateCmd.VibrateSubcommand> speeds = new ArrayList<>();
            for (int i = 0; i < Lovense.this.vibratorCount; ++i) {
                speeds.add(vibrateCmd.new VibrateSubcommand(i, cmdMsg.getSpeed()));
            }
            vibrateCmd.speeds = speeds;
            return Lovense.this.handleVibrateCmd.invoke(vibrateCmd);
        }
    };

    private IButtplugDeviceMessageCallback handleVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof VibrateCmd)) {
                return Lovense.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            VibrateCmd cmdMsg = (VibrateCmd) msg;

            if (cmdMsg.speeds.size() < 1 || cmdMsg.speeds.size() > Lovense.this.vibratorCount) {
                return new Error(String.format("VibrateCmd requires %s for this device.",
                        Lovense.this.vibratorCount == 1 ? "1 vector" : String.format(
                                "between 1 and %s vectors",
                                Lovense.this.vibratorCount
                        )), Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
            }

            for (VibrateCmd.VibrateSubcommand speed : cmdMsg.speeds) {
                if (speed.index >= Lovense.this.vibratorCount) {
                    return new Error(String.format("Index %s is out of bounds for VibrateCmd for this device.",
                            speed.index), Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
                }

                if (Math.abs(Lovense.this.vibratorSpeeds[(int) speed.index] - speed.getSpeed()) < 0.0001) {
                    continue;
                }

                Lovense.this.vibratorSpeeds[(int) speed.index] = speed.getSpeed();
                try {
                    ButtplugMessage res = Lovense.this.iface.writeValue(
                            cmdMsg.id,
                            UUID.fromString(Lovense.this.info.getCharacteristics().get(LovenseRev1BluetoothInfo.Chrs.Tx.ordinal())),
                            String.format("Vibrate%s:%s;",
                                    Lovense.this.vibratorCount == 1 ? "" : speed.index + 1,
                                    (int) (speed.getSpeed() * 20)).getBytes()).get();
                    if (!(res instanceof Ok)) {
                        return res;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    return Lovense.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                            "Exception writing value");
                }
            }
            return new Ok(cmdMsg.id);
        }
    };

    private IButtplugDeviceMessageCallback handleRotateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof RotateCmd)) {
                return Lovense.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            RotateCmd cmdMsg = (RotateCmd) msg;

            boolean dirChange = false;
            boolean speedChange = false;

            if (cmdMsg.rotations.size() != 1) {
                return new Error("RotateCmd requires 1 vector for this device.",
                        Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
            }

            for (RotateCmd.RotateSubcommand v : cmdMsg.rotations) {
                if (v.index != 0) {
                    return new Error(String.format("Index %s is out of bounds for RotateCmd for this device.",
                            v.index), Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
                }

                speedChange = Math.abs(Lovense.this.rotateSpeed - v.getSpeed()) > 0.0001;
                Lovense.this.rotateSpeed = v.getSpeed();
                dirChange = Lovense.this.clockwise != v.clockwise;
            }

            if (dirChange) {
                Lovense.this.clockwise = !Lovense.this.clockwise;
                try {
                    Lovense.this.iface.writeValue(
                            cmdMsg.id,
                            UUID.fromString(Lovense.this.info.getCharacteristics().get(LovenseRev1BluetoothInfo.Chrs.Tx.ordinal())),
                            "RotateChange;".getBytes()).get();
                } catch (InterruptedException | ExecutionException e) {
                    return Lovense.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                            "Exception writing value");
                }
            }

            if (!speedChange)
            {
                return new Ok(cmdMsg.id);
            }

            try {
                return Lovense.this.iface.writeValue(
                        cmdMsg.id,
                        UUID.fromString(Lovense.this.info.getCharacteristics().get(LovenseRev1BluetoothInfo.Chrs.Tx.ordinal())),
                        String.format("Rotate:%s;", Lovense.this.rotateSpeed * 20).getBytes()).get();
            } catch (InterruptedException | ExecutionException e) {
                return Lovense.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Exception writing value");
            }
        }
    };
}
