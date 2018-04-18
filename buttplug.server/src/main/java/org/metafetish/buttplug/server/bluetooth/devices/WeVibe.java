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
import java.util.List;
import java.util.concurrent.ExecutionException;


public class WeVibe extends ButtplugBluetoothDevice {
    private static List<String> dualVibes = new ArrayList<String>() {{
        add("Cougar");
        add("4 Plus");
        add("4plus");
        add("classic");
        add("Gala");
        add("Nova");
        add("NOVAV2");
        add("Sync");
    }};
    private int vibratorCount = 1;
    private double[] vibratorSpeeds = {0, 0};

    public WeVibe(IBluetoothDeviceInterface iface,
                   IBluetoothDeviceInfo info) {
        super(String.format("WeVibe Device %s", iface.getName()), iface, info);
        if (WeVibe.dualVibes.contains(iface.getName())) {
            ++vibratorCount;
        }

        msgFuncs.put(SingleMotorVibrateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleSingleMotorVibrateCmd));
        msgFuncs.put(VibrateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleVibrateCmd, new MessageAttributes(vibratorCount)));
        msgFuncs.put(StopDeviceCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleStopDeviceCmd));
    }

    private IButtplugDeviceMessageCallback handleStopDeviceCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            WeVibe.this.bpLogger.debug(String.format("Stopping Device %s", WeVibe.this.getName()));
            return WeVibe.this.handleSingleMotorVibrateCmd.invoke(new SingleMotorVibrateCmd(msg.deviceIndex, 0, msg.id));
        }
    };

    private IButtplugDeviceMessageCallback handleSingleMotorVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof SingleMotorVibrateCmd)) {
                return WeVibe.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");
            }
            SingleMotorVibrateCmd cmdMsg = (SingleMotorVibrateCmd) msg;

            VibrateCmd vibrateCmd = new VibrateCmd(cmdMsg.deviceIndex, null, cmdMsg.id);
            ArrayList<VibrateCmd.VibrateSubcommand> speeds = new ArrayList<>();
            for (int i = 0; i < WeVibe.this.vibratorCount; ++i) {
                speeds.add(vibrateCmd.new VibrateSubcommand(i, cmdMsg.getSpeed()));
            }
            vibrateCmd.speeds = speeds;
            return WeVibe.this.handleVibrateCmd.invoke(vibrateCmd);
        }
    };

    private IButtplugDeviceMessageCallback handleVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof VibrateCmd)) {
                return WeVibe.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            VibrateCmd cmdMsg = (VibrateCmd) msg;

            if (cmdMsg.speeds.size() < 1 || cmdMsg.speeds.size() > WeVibe.this.vibratorCount) {
                return new Error(String.format("VibrateCmd requires %s for this device.",
                        WeVibe.this.vibratorCount == 1 ? "1 vector" : String.format(
                                "between 1 and %s vectors",
                                WeVibe.this.vibratorCount
                        )), Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
            }

            boolean changed = false;
            for (VibrateCmd.VibrateSubcommand speed : cmdMsg.speeds) {
                if (speed.index >= WeVibe.this.vibratorCount) {
                    return new Error(String.format("Index %s is out of bounds for VibrateCmd for this device.",
                            speed.index), Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
                }

                if (Math.abs(WeVibe.this.vibratorSpeeds[(int) speed.index] - speed.getSpeed()) < 0.0001) {
                    continue;
                }

                changed = true;
                WeVibe.this.vibratorSpeeds[(int) speed.index] = speed.getSpeed();
            }
            if (!changed) {
                return new Ok(cmdMsg.id);
            }

            int rSpeedInt = (int) (WeVibe.this.vibratorSpeeds[0] * 15);
            int rSpeedExt = (int) (WeVibe.this.vibratorSpeeds[WeVibe.this.vibratorCount - 1] * 15);

            // 0f 03 00 bc 00 00 00 00
            byte[] data = new byte[] {0x0f, 0x03, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00};
            data[3] = (byte) rSpeedExt; // External
            data[3] |= (byte) (rSpeedInt << 4); // Internal

            if (rSpeedInt == 0 && rSpeedExt == 0) {
                data[1] = 0x00;
                data[5] = 0x00;
            }

            try {
                ButtplugMessage res = WeVibe.this.iface.writeValue(
                        cmdMsg.id,
                        WeVibe.this.info.getCharacteristics().get(WeVibeBluetoothInfo.Chrs.Tx.ordinal()),
                        data).get();
                if (!(res instanceof Ok)) {
                    return res;
                }
            } catch (InterruptedException | ExecutionException e) {
                return WeVibe.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Exception writing value");
            }
            return new Ok(cmdMsg.id);
        }
    };
}
