package org.metafetish.buttplug.server.bluetooth.devices;

import android.support.annotation.NonNull;

import org.metafetish.buttplug.core.ButtplugDeviceMessage;
import org.metafetish.buttplug.core.ButtplugMessage;
import org.metafetish.buttplug.core.IButtplugDeviceMessageCallback;
import org.metafetish.buttplug.core.IButtplugLogManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class KiirooGen2Vibe extends ButtplugBluetoothDevice {
    private double[] vibratorSpeeds = {0, 0, 0};

    private static class KiirooGen2VibeType {
        public String brand;
        public int vibeCount;
        public int[] vibeOrder;

        public KiirooGen2VibeType(String brand, int vibeCount, int[] vibeOrder) {
            this.brand = brand;
            this.vibeCount = vibeCount;
            this.vibeOrder = vibeOrder;
        }
    }

    private static Map<String, KiirooGen2VibeType> devInfos = new HashMap<String, KiirooGen2VibeType>() {{
        put("Pearl2", new KiirooGen2VibeType("Kiiroo", 1, new int[] {0, 1, 2}));
        put("Fuse", new KiirooGen2VibeType("OhMiBod", 2, new int[] {1, 0, 2}));
        put("Virtual Blowbot", new KiirooGen2VibeType("PornHub", 3, new int[] {0, 1, 2}));
    }};

    private KiirooGen2VibeType devInfo;

    public KiirooGen2Vibe(@NonNull IButtplugLogManager aLogManager,
                          @NonNull IBluetoothDeviceInterface aInterface,
                          @NonNull IBluetoothDeviceInfo aInfo) {
        super(aLogManager, devInfos.get(aInterface.getName()) + " " + aInterface.getName(), aInterface, aInfo);
        this.devInfo = this.devInfos.get(aInterface.getName());
        msgFuncs.put(StopDeviceCmd.class, new ButtplugDeviceWrapper(this.handleStopDeviceCmd));
        msgFuncs.put(VibrateCmd.class, new ButtplugDeviceWrapper(this.handleVibrateCmd, new MessageAttributes(devInfo.vibeCount)));
        msgFuncs.put(SingleMotorVibrateCmd.class, new ButtplugDeviceWrapper(this.handleSingleMotorVibrateCmd));
    }

    private IButtplugDeviceMessageCallback handleStopDeviceCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            KiirooGen2Vibe.this.bpLogger.debug("Stopping Device " + KiirooGen2Vibe.this.getName());

            VibrateCmd vibrateCmd = new VibrateCmd(msg.deviceIndex, null, msg.id);
            List<VibrateCmd.VibrateSubcommand> vCmds = new ArrayList<VibrateCmd.VibrateSubcommand>();
            for (int i = 0; i < KiirooGen2Vibe.this.devInfo.vibeCount; i++) {
                vCmds.add(vibrateCmd.new VibrateSubcommand(i, 0));
            }
            vibrateCmd.speeds = vCmds;

            return KiirooGen2Vibe.this.handleVibrateCmd.invoke(vibrateCmd);
        }
    };

    private IButtplugDeviceMessageCallback handleSingleMotorVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof SingleMotorVibrateCmd)) {
                return KiirooGen2Vibe.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");
            }
            SingleMotorVibrateCmd cmdMsg = (SingleMotorVibrateCmd) msg;

            if (Math.abs(KiirooGen2Vibe.this.vibratorSpeeds[0] - cmdMsg.getSpeed()) < 0.001) {
                return new Ok(cmdMsg.id);
            }

            VibrateCmd vibrateCmd = new VibrateCmd(msg.deviceIndex, null, msg.id);
            List<VibrateCmd.VibrateSubcommand> vCmds = new ArrayList<VibrateCmd.VibrateSubcommand>();
            for (int i = 0; i < KiirooGen2Vibe.this.devInfo.vibeCount; i++) {
                vCmds.add(vibrateCmd.new VibrateSubcommand(i, cmdMsg.getSpeed()));
            }
            vibrateCmd.speeds = vCmds;

            return KiirooGen2Vibe.this.handleVibrateCmd.invoke(vibrateCmd);
        }
    };

    private IButtplugDeviceMessageCallback handleVibrateCmd = new IButtplugDeviceMessageCallback() {
        @Override
        public ButtplugMessage invoke(ButtplugDeviceMessage msg) {
            if (!(msg instanceof VibrateCmd)) {
                return KiirooGen2Vibe.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Wrong Handler");

            }
            VibrateCmd cmdMsg = (VibrateCmd) msg;

            if (cmdMsg.speeds.size() < 1 || cmdMsg.speeds.size() > KiirooGen2Vibe.this.devInfo.vibeCount) {
                return new Error(
                        "VibrateCmd requires between 1 and " + KiirooGen2Vibe.this.devInfo.vibeCount + " vectors for this device.",
                        Error.ErrorClass.ERROR_DEVICE,
                        cmdMsg.id);
            }

            boolean changed = false;
            for (VibrateCmd.VibrateSubcommand vi : cmdMsg.speeds) {
                if (vi.index >= KiirooGen2Vibe.this.devInfo.vibeCount) {
                    return new Error(
                            "Index " + vi.index + " is out of bounds for VibrateCmd for this device.",
                            Error.ErrorClass.ERROR_DEVICE,
                            cmdMsg.id);
                }

                if (Math.abs(KiirooGen2Vibe.this.vibratorSpeeds[(int) vi.index] - vi.getSpeed()) < 0.0001) {
                    continue;
                }

                KiirooGen2Vibe.this.vibratorSpeeds[(int) vi.index] = vi.getSpeed();
                changed = true;
            }

            if (!changed) {
                return new Ok(cmdMsg.id);
            }

            byte[] data = {
                    (byte) (KiirooGen2Vibe.this.vibratorSpeeds[KiirooGen2Vibe.this.devInfo.vibeOrder[0]] * 100),
                    (byte) (KiirooGen2Vibe.this.vibratorSpeeds[KiirooGen2Vibe.this.devInfo.vibeOrder[1]] * 100),
                    (byte) (KiirooGen2Vibe.this.vibratorSpeeds[KiirooGen2Vibe.this.devInfo.vibeOrder[2]] * 100)
            };

            try {
                return KiirooGen2Vibe.this.iface.writeValue(
                        cmdMsg.id,
                        KiirooGen2Vibe.this.info.getCharacteristics().get(KiirooGen2VibeBluetoothInfo.Chrs.Tx.ordinal()),
                        data
                ).get();
            } catch (InterruptedException | ExecutionException e) {
                return KiirooGen2Vibe.this.bpLogger.logErrorMsg(msg.id, Error.ErrorClass.ERROR_DEVICE,
                        "Exception writing value");
            }
        }
    };
}
