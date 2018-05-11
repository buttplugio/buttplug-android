package org.metafetish.buttplug.server.bluetooth.devices;

import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugDeviceMessageCallback;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.RotateCmd;
import org.metafetish.buttplug.core.Messages.StopDeviceCmd;
import org.metafetish.buttplug.core.Messages.VorzeA10CycloneCmd;
import org.metafetish.buttplug.server.bluetooth.ButtplugBluetoothDevice;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInfo;
import org.metafetish.buttplug.server.bluetooth.IBluetoothDeviceInterface;

import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class VorzeA10Cyclone extends ButtplugBluetoothDevice {
    private boolean clockwise = true;
    private int speed;

    public VorzeA10Cyclone(IBluetoothDeviceInterface iface,
                   IBluetoothDeviceInfo info) {
        super("Vorze A10 Cyclone", iface, info);

        msgFuncs.put(VorzeA10CycloneCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleVorzeA10CycloneCmd));
        msgFuncs.put(RotateCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleRotateCmd, new MessageAttributes(1)));
        msgFuncs.put(StopDeviceCmd.class.getSimpleName(), new ButtplugDeviceWrapper(this.handleStopDeviceCmd));
    }

    private IButtplugDeviceMessageCallback handleStopDeviceCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            VorzeA10Cyclone.this.bpLogger.debug(
                    String.format("Stopping Device %s", VorzeA10Cyclone.this.getName()));
            return VorzeA10Cyclone.this.handleVorzeA10CycloneCmd.invoke(
                    new VorzeA10CycloneCmd(msg.deviceIndex, 0, VorzeA10Cyclone.this.clockwise, msg.id));
        }
    };

    private IButtplugDeviceMessageCallback handleRotateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof RotateCmd)) {
                return VorzeA10Cyclone.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            RotateCmd cmdMsg = (RotateCmd) msg;

            if (cmdMsg.rotations.size() != 1) {
                return new Error("RotateCmd requires 1 vector for this device.",
                        Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
            }

            for (RotateCmd.RotateSubcommand v : cmdMsg.rotations) {
                if (v.index != 0) {
                    return new Error(String.format("Index %s is out of bounds for RotateCmd for this device.",
                            v.index), Error.ErrorClass.ERROR_DEVICE, cmdMsg.id);
                }

                return VorzeA10Cyclone.this.handleVorzeA10CycloneCmd.invoke(new VorzeA10CycloneCmd(cmdMsg.deviceIndex,
                        (int) (v.getSpeed() * 99), v.clockwise, cmdMsg.id));
            }
            return new Ok(cmdMsg.id);
        }
    };

    private IButtplugDeviceMessageCallback handleVorzeA10CycloneCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof VorzeA10CycloneCmd)) {
                return VorzeA10Cyclone.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            VorzeA10CycloneCmd cmdMsg = (VorzeA10CycloneCmd) msg;

            if (VorzeA10Cyclone.this.clockwise == cmdMsg.isClockwise() && VorzeA10Cyclone.this.speed == cmdMsg.getSpeed()) {
                return new Ok(cmdMsg.id);
            }

            VorzeA10Cyclone.this.clockwise = cmdMsg.isClockwise();
            VorzeA10Cyclone.this.speed = cmdMsg.getSpeed();


            byte rawSpeed = (byte)((byte)(VorzeA10Cyclone.this.clockwise ? 1 : 0) << 7 | (byte) VorzeA10Cyclone.this.speed);

            byte[] data = new byte[] {0x01, 0x01, rawSpeed};

            try {
                return VorzeA10Cyclone.this.iface.writeValue(
                        cmdMsg.id,
                        UUID.fromString(VorzeA10Cyclone.this.info.getCharacteristics().get(VorzeA10CycloneInfo.Chrs.Tx.ordinal())),
                        data).get();
            } catch (InterruptedException | ExecutionException e) {
                return VorzeA10Cyclone.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Exception writing value");
            }
        }
    };
}
