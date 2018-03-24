package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"Id", "DeviceIndex", "Speeds"})
public class VibrateCmd extends ButtplugDeviceMessage {

    @JsonPropertyOrder({"Index", "Speed"})
    public class VibrateSubcommand {

        private double speed;

        @JsonProperty(value = "Index", required = true)
        public long index;

        @JsonProperty(value = "Speed", required = true)
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

        @SuppressWarnings("unused")
        public VibrateSubcommand() {
            this.index = -1;
            this.setSpeed(0);
        }

        public VibrateSubcommand(long index, double speed) {
            this.index = index;
            this.setSpeed(speed);
        }
    }

    @JsonProperty(value = "Speeds", required = true)
    @JsonDeserialize(contentUsing = VibrateSubcommandDeserializer.class)
    public ArrayList<VibrateSubcommand> speeds;

    @SuppressWarnings("unused")
    private VibrateCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        this.speeds = new ArrayList<>();
    }

    public VibrateCmd(long deviceIndex, ArrayList<VibrateSubcommand> speeds) {
        this(deviceIndex, speeds, ButtplugConsts.DefaultMsgId);
    }

    public VibrateCmd(long deviceIndex, ArrayList<VibrateSubcommand> speeds, long id) {
        super(id, deviceIndex);
        this.speeds = speeds;
    }
}
