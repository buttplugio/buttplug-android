package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"Id", "DeviceIndex", "Rotations"})
public class RotateCmd extends ButtplugDeviceMessage {

    @JsonPropertyOrder({"Index", "Speed", "Clockwise"})
    public class RotateSubcommand {
        private double speed;

        @JsonProperty(value = "Index", required = true)
        public long index;

        @JsonProperty(value = "Speed", required = true)
        public double getSpeed() {
            return this.speed;
        }

        public void setSpeed(double speed) {
            if (speed < 0) {
                throw new IllegalArgumentException("RotateCmd Speed cannot be less than 0!");
            }

            if (speed > 1) {
                throw new IllegalArgumentException("RotateCmd Speed cannot be greater than 1!");
            }
            this.speed = speed;
        }

        @JsonProperty(value = "Clockwise", required = true)
        public boolean clockwise;

        @SuppressWarnings("unused")
        public RotateSubcommand() {
            this.index = -1;
            this.setSpeed(0);
            this.clockwise = false;
        }

        public RotateSubcommand(long index, double speed, boolean clockwise) {
            this.index = index;
            this.setSpeed(speed);
            this.clockwise = clockwise;
        }
    }

    @JsonProperty(value = "Rotations", required = true)
    @JsonDeserialize(contentUsing = RotateSubcommandDeserializer.class)
    public ArrayList<RotateSubcommand> rotations;

    @SuppressWarnings("unused")
    private RotateCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        this.rotations = new ArrayList<>();
    }

    public RotateCmd(long deviceIndex, ArrayList<RotateSubcommand> rotations) {
        this(deviceIndex, rotations, ButtplugConsts.DefaultMsgId);
    }

    public RotateCmd(long deviceIndex, ArrayList<RotateSubcommand> rotations, long id) {
        super(id, deviceIndex);
        this.rotations = rotations;
    }
}
