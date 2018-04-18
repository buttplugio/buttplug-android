package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

@JsonPropertyOrder({"Id", "DeviceIndex", "Speed", "Clockwise"})
public class VorzeA10CycloneCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Speed", required = true)
    private int speed;

    @JsonProperty(value = "Clockwise", required = true)
    public boolean clockwise;

    public VorzeA10CycloneCmd(long deviceIndex, int speed, boolean clockwise, long id) {
        super(id, deviceIndex);
        setSpeed(speed);
        this.clockwise = clockwise;
    }

    @SuppressWarnings("unused")
    public VorzeA10CycloneCmd(int speed, boolean clockwise) {
        super(ButtplugConsts.DefaultMsgId, -1);
        setSpeed(speed);
        this.clockwise = clockwise;
    }

    @SuppressWarnings("unused")
    private VorzeA10CycloneCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        setSpeed(0);
        this.clockwise = false;
    }

    public int getSpeed() {
        if (speed > 99 || speed < 0) {
            return 0;
        }
        return speed;
    }

    public void setSpeed(int speed) {
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
