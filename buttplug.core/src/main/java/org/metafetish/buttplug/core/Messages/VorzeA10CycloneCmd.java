package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VorzeA10CycloneCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Speed", required = true)
    private int speed;

    public int GetSpeed() {
        if (speed > 99 || speed < 0) {
            return 0;
        }
        return speed;
    }

    public void SetSpeed(int speed) {
        if (speed > 99) {
            throw new IllegalArgumentException(
                    "VorzeA10CycloneCmd cannot have a speed higher than 99!");
        }

        if (speed < 0) {
            throw new IllegalArgumentException(
                    "VorzeA10CycloneCmd cannot have a speed lower than 0!");
        }

        this.speed = speed;
    }

    @JsonProperty(value = "Clockwise", required = true)
    public boolean clockwise;

    public VorzeA10CycloneCmd(long deviceIndex, int speed, boolean clockwise, long id) {
        super(id, deviceIndex);
        SetSpeed(speed);
        this.clockwise = clockwise;
    }

    public VorzeA10CycloneCmd(long deviceIndex, int speed, boolean clockwise) {
        super(ButtplugConsts.DefaultMsgId, deviceIndex);
        SetSpeed(speed);
        this.clockwise = clockwise;
    }
}
