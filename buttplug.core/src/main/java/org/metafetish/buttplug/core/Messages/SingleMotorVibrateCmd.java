package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

@JsonPropertyOrder({"Id", "DeviceIndex", "Speed"})
public class SingleMotorVibrateCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Speed", required = true)
    private double speed;

    public SingleMotorVibrateCmd(long deviceIndex, double speed, long id) {
        super(id, deviceIndex);
        setSpeed(speed);
    }

    @SuppressWarnings("unused")
    private SingleMotorVibrateCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        setSpeed(0);
    }

    public double getSpeed() {
        if (speed > 1 || speed < 0) {
            return 0;
        }
        return speed;
    }

    public void setSpeed(double speed) {
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
}
