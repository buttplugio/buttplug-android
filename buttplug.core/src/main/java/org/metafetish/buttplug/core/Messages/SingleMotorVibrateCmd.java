package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SingleMotorVibrateCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Speed", required = true)
    private double speed;

    public double GetSpeed() {
        if (speed > 1 || speed < 0) {
            return 0;
        }
        return speed;
    }

    public void SetSpeed(double speed) {
        if (speed > 1) {
            throw new IllegalArgumentException(
                    "SingleMotorVibrateCmd cannot have a speed higher than 1!");
        }

        if (speed < 0) {
            throw new IllegalArgumentException(
                    "SingleMotorVibrateCmd cannot have a speed lower than 0!");
        }

        this.speed = speed;
    }

    public SingleMotorVibrateCmd(long deviceIndex, double speed, long id) {
        super(id, deviceIndex);
        SetSpeed(speed);
    }

    public SingleMotorVibrateCmd(long deviceIndex, double speed) {
        super(ButtplugConsts.DefaultMsgId, deviceIndex);
        SetSpeed(speed);
    }
}
