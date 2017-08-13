package org.metafetish.buttplug.core.Messages;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FleshlightLaunchFW12Cmd extends ButtplugDeviceMessage {

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
                    "FleshlightLaunchFW12Cmd cannot have a speed higher than 99!");
        }

        if (speed < 0) {
            throw new IllegalArgumentException(
                    "FleshlightLaunchFW12Cmd cannot have a speed lower than 0!");
        }

        this.speed = speed;
    }
    
    @JsonProperty(value = "Position", required = true)
    private int position;

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


    public FleshlightLaunchFW12Cmd(long deviceIndex, int speed, int position, long id) {
        super(id, deviceIndex);

        SetSpeed(speed);
        SetPosition(position);
    }

    public FleshlightLaunchFW12Cmd(long deviceIndex, int speed, int position) {
        super(ButtplugConsts.DefaultMsgId, deviceIndex);

        this.speed = speed;
        SetSpeed(speed);
        SetPosition(position);
    }
}
