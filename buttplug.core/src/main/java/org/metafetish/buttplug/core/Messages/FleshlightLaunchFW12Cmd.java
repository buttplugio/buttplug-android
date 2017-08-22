package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

public class FleshlightLaunchFW12Cmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Speed", required = true)
    private int speed;
    @JsonProperty(value = "Position", required = true)
    private int position;

    public FleshlightLaunchFW12Cmd(long deviceIndex, int speed, int position, long id) {
        super(id, deviceIndex);

        SetSpeed(speed);
        SetPosition(position);
    }

    @SuppressWarnings("unused")
    private FleshlightLaunchFW12Cmd() {
        super(ButtplugConsts.DefaultMsgId, -1);

        SetSpeed(0);
        SetPosition(0);
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
                    "FleshlightLaunchFW12Cmd cannot have a speed higher than 99!");
        }

        if (speed < 0) {
            throw new IllegalArgumentException(
                    "FleshlightLaunchFW12Cmd cannot have a speed lower than 0!");
        }

        this.speed = speed;
    }

    public int GetPosition() {
        if (position > 99 || position < 0) {
            return 0;
        }
        return position;
    }

    public void SetPosition(int position) {
        if (position > 99) {
            throw new IllegalArgumentException(
                    "FleshlightLaunchFW12Cmd cannot have a position higher than 99!");
        }

        if (position < 0) {
            throw new IllegalArgumentException(
                    "FleshlightLaunchFW12Cmd cannot have a position lower than 0!");
        }

        this.position = position;
    }
}
