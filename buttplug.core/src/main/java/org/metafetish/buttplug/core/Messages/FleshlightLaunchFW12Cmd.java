package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

@JsonPropertyOrder({"Id", "DeviceIndex", "Position", "Speed"})
public class FleshlightLaunchFW12Cmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Position", required = true)
    private int position;

    @JsonProperty(value = "Speed", required = true)
    private int speed;

    public FleshlightLaunchFW12Cmd(long deviceIndex, int speed, int position, long id) {
        super(id, deviceIndex);

        setSpeed(speed);
        setPosition(position);
    }

    @SuppressWarnings("unused")
    private FleshlightLaunchFW12Cmd() {
        super(ButtplugConsts.DefaultMsgId, -1);

        setSpeed(0);
        setPosition(0);
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
                    "FleshlightLaunchFW12Cmd cannot have a speed higher than 99!");
        }

        if (speed < 0) {
            throw new IllegalArgumentException(
                    "FleshlightLaunchFW12Cmd cannot have a speed lower than 0!");
        }

        this.speed = speed;
    }

    public int getPosition() {
        if (position > 99 || position < 0) {
            return 0;
        }
        return position;
    }

    public void setPosition(int position) {
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
