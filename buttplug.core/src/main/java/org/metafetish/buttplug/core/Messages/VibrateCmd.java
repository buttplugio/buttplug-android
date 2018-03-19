package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.List;


public class VibrateCmd extends ButtplugDeviceMessage {
    public class VibrateSubcommand {
        private double speed;

        @JsonProperty(required = true)
        public long index;

        @JsonProperty(required = true)
        public double getSpeed() {
            return this.speed;
        }

        public void setSpeed(double speed) {
            if (speed < 0) {
                throw new IllegalArgumentException("VibrateCmd Speed cannot be less than 0!");
            }

            if (speed > 1) {
                throw new IllegalArgumentException("VibrateCmd Speed cannot be greater than 1!");
            }
            this.speed = speed;
        }

        public VibrateSubcommand(long index, double speed) {
            this.index = index;
            this.setSpeed(speed);
        }
    }

    @JsonProperty(required = true)
    public List<VibrateSubcommand> speeds;

    public VibrateCmd(long deviceIndex, List<VibrateSubcommand> speeds) {
        this(deviceIndex, speeds, ButtplugConsts.DefaultMsgId);
    }

    public VibrateCmd(long deviceIndex, List<VibrateSubcommand> speeds, long id) {
        super(id, deviceIndex);
        this.speeds = speeds;
    }
}
