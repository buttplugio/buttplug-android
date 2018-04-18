package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugDeviceMessageCallback;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.SingleMotorVibrateCmd;
import org.metafetish.buttplug.core.Messages.StopDeviceCmd;
import org.metafetish.buttplug.core.Messages.VibrateCmd;
import org.metafetish.buttplug.server.bluetooth.ButtplugBluetoothDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MagicMotion extends ButtplugBluetoothDevice {
    private double vibratorSpeed;

    public MagicMotion(@NonNull IBluetoothDeviceInterface iface,
                  @NonNull IBluetoothDeviceInfo info) {
        super(String.format("MagicMotion %s", iface.getName()), iface, info);
        msgFuncs.put(SingleMotorVibrateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(
                this.handleSingleMotorVibrateCmd, new MessageAttributes(1)));
        msgFuncs.put(VibrateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleVibrateCmd,
                new MessageAttributes(1)));
        msgFuncs.put(StopDeviceCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleStopDeviceCmd));
    }

    private IButtplugDeviceMessageCallback handleStopDeviceCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            MagicMotion.this.bpLogger.debug(
                    String.format("Stopping Device %s", MagicMotion.this.getName()));
            return MagicMotion.this.handleSingleMotorVibrateCmd.invoke(
                    new SingleMotorVibrateCmd(msg.deviceIndex, 0, msg.id));
        }
    };

    private IButtplugDeviceMessageCallback handleSingleMotorVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof SingleMotorVibrateCmd)) {
                return MagicMotion.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");
            }
            SingleMotorVibrateCmd cmdMsg = (SingleMotorVibrateCmd) msg;

            VibrateCmd vibrateCmd = new VibrateCmd(cmdMsg.deviceIndex, null, cmdMsg.id);
            ArrayList<VibrateCmd.VibrateSubcommand> speeds = new ArrayList<>();
            speeds.add(vibrateCmd.new VibrateSubcommand(0, cmdMsg.getSpeed()));
            vibrateCmd.speeds = speeds;
            return MagicMotion.this.handleVibrateCmd.invoke(vibrateCmd);
        }
    };

    private IButtplugDeviceMessageCallback handleVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof VibrateCmd)) {
                return MagicMotion.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            VibrateCmd cmdMsg = (VibrateCmd) msg;

            if (cmdMsg.speeds.size() != 1) {
                return new Error("VibrateCmd requires 1 vector for this device.", Error
                        .ErrorClass.ERROR_DEVICE, cmdMsg.id);
            }


            for (VibrateCmd.VibrateSubcommand speed : cmdMsg.speeds) {
                if (speed.index != 1) {
                    return new Error(String.format("Index %s is out of bounds for VibrateCmd for this device.",
                            speed.index), Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
                }

                if (Math.abs(MagicMotion.this.vibratorSpeed - speed.getSpeed()) < 0.001) {
                    return new Ok(cmdMsg.id);
                }

                MagicMotion.this.vibratorSpeed = speed.getSpeed();
            }

            byte[] data = new byte[] {0x0b, (byte) 0xff, 0x04, 0x0a, 0x32, 0x32, 0x00, 0x04, 0x08, 0x00, 0x64, 0x00};
            data[9] = (byte) (MagicMotion.this.vibratorSpeed * 0xff);

            try {
                return MagicMotion.this.iface.writeValue(
                        cmdMsg.id,
                        MagicMotion.this.info.getCharacteristics().get(
                                MagicMotionBluetoothInfo.Chrs.Tx.ordinal()),
                        data
                ).get();
            } catch (InterruptedException | ExecutionException e) {
                return MagicMotion.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Exception writing value");
            }
        }
    };
}
