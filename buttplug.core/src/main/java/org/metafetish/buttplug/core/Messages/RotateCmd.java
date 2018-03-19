package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.List;


public class RotateCmd extends ButtplugDeviceMessage {
    public class RotateSubcommand {
        private double speed;

        @JsonProperty(required = true)
        public long index;

        @JsonProperty(required = true)
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

        @JsonProperty(required = true)
        public boolean clockwise;

        public RotateSubcommand(long index, double speed, boolean clockwise) {
            this.index = index;
            this.setSpeed(speed);
            this.clockwise = clockwise;
        }
    }

    @JsonProperty(required = true)
    public List<RotateSubcommand> rotations;

    public RotateCmd(long deviceIndex, List<RotateSubcommand> rotations) {
        this(deviceIndex, rotations, ButtplugConsts.DefaultMsgId);
    }

    public RotateCmd(long deviceIndex, List<RotateSubcommand> rotations, long id) {
        super(id, deviceIndex);
        this.rotations = rotations;
    }
}
