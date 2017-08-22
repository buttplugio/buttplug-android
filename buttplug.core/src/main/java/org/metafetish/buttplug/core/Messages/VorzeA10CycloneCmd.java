package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

public class VorzeA10CycloneCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Clockwise", required = true)
    public boolean clockwise;
    @JsonProperty(value = "Speed", required = true)
    private int speed;

    public VorzeA10CycloneCmd(long deviceIndex, int speed, boolean clockwise, long id) {
        super(id, deviceIndex);
        SetSpeed(speed);
        this.clockwise = clockwise;
    }

    @SuppressWarnings("unused")
    private VorzeA10CycloneCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        SetSpeed(0);
        this.clockwise = false;
    }

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
}
