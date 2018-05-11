package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugDeviceMessageCallback;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.KiirooCmd;
import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.SingleMotorVibrateCmd;
import org.metafetish.buttplug.core.Messages.StopDeviceCmd;
import org.metafetish.buttplug.core.Messages.VibrateCmd;
import org.metafetish.buttplug.server.bluetooth.ButtplugBluetoothDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class Kiiroo extends ButtplugBluetoothDevice {
    private double vibratorSpeed;

    public Kiiroo(@NonNull IBluetoothDeviceInterface iface,
                  @NonNull IBluetoothDeviceInfo info) {
        super(String.format("Kiiroo %s", iface.getName()), iface, info);
        // Setup message function array
        msgFuncs.put(KiirooCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleKiirooRawCmd));
        msgFuncs.put(StopDeviceCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleStopDeviceCmd));
        if (iface.getName().equals("PEARL")) {
            msgFuncs.put(VibrateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleVibrateCmd, new
                    MessageAttributes(1)));

            msgFuncs.put(SingleMotorVibrateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this
                    .handleSingleMotorVibrateCmd, new MessageAttributes(1)));
        }
    }

    private IButtplugDeviceMessageCallback handleStopDeviceCmd = new
            IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            // Right now, this is a nop. The Onyx doesn't have any sort of permanent movement state,
            // and its longest movement is like 150ms or so. The Pearl is supposed to vibrate but
            // I've

            // never gotten that to work. So for now, we just return ok.
            Kiiroo.this.bpLogger.debug(String.format("Stopping Device %s", Kiiroo.this.getName()));

            if (Kiiroo.this.iface.getName().equals("PEARL") && Kiiroo.this.vibratorSpeed > 0) {
                return Kiiroo.this.handleKiirooRawCmd.invoke(new KiirooCmd(msg.deviceIndex, 0,
                        msg.id));
            }

            return new Ok(msg.id);
        }
    };

    private IButtplugDeviceMessageCallback handleKiirooRawCmd = new
            IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof KiirooCmd)) {
                return Kiiroo.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            KiirooCmd cmdMsg = (KiirooCmd) msg;

            try {
                return Kiiroo.this.iface.writeValue(
                        cmdMsg.id,
                        UUID.fromString(Kiiroo.this.info.getCharacteristics().get(KiirooBluetoothInfo.Chrs.Tx.ordinal())),
                        (String.format("%s,\n", cmdMsg.getPosition())).getBytes()
                ).get();
            } catch (InterruptedException | ExecutionException e) {
                return Kiiroo.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Exception writing value");
            }
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private IButtplugDeviceMessageCallback handleSingleMotorVibrateCmd = new
            IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof SingleMotorVibrateCmd)) {
                return Kiiroo.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");
            }
            SingleMotorVibrateCmd cmdMsg = (SingleMotorVibrateCmd) msg;

            if (Math.abs(Kiiroo.this.vibratorSpeed - cmdMsg.getSpeed()) < 0.001) {
                return new Ok(cmdMsg.id);
            }

            Kiiroo.this.vibratorSpeed = cmdMsg.getSpeed();

            VibrateCmd vibrateCmd = new VibrateCmd(cmdMsg.deviceIndex, null, cmdMsg.id);
            ArrayList<VibrateCmd.VibrateSubcommand> speeds = new ArrayList<>();
            speeds.add(vibrateCmd.new VibrateSubcommand(0, cmdMsg.getSpeed()));
            vibrateCmd.speeds = speeds;
            return Kiiroo.this.handleVibrateCmd.invoke(vibrateCmd);
        }
    };

    private IButtplugDeviceMessageCallback handleVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof VibrateCmd)) {
                return Kiiroo.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            VibrateCmd cmdMsg = (VibrateCmd) msg;

            if (cmdMsg.speeds.size() != 1) {
                return new Error("VibrateCmd requires 1 vector for this device.", Error
                        .ErrorClass.ERROR_DEVICE, cmdMsg.id);
            }

            VibrateCmd.VibrateSubcommand speed = cmdMsg.speeds.get(0);
            if (speed.index != 0) {
                return new Error(String.format("Index %s is out of bounds for VibrateCmd for this device.",
                        speed.index), Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
            }
            Kiiroo.this.vibratorSpeed = speed.getSpeed();

            return Kiiroo.this.handleKiirooRawCmd.invoke(new KiirooCmd(msg.deviceIndex, (int)
                    (Kiiroo.this.vibratorSpeed * 4), msg.id));
        }
    };

}
