package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"Id", "DeviceIndex", "Vectors"})
public class LinearCmd extends ButtplugDeviceMessage {

    @JsonPropertyOrder({"Index", "Duration", "Position"})
    public class VectorSubcommand {

        private double position;

        @JsonProperty(value = "Index", required = true)
        public long index;

        @JsonProperty(value = "Duration", required = true)
        public long duration;

        //TODO: Be more consistent with use of public properties or getter/setters
        @JsonProperty(value = "Position", required = true)
        public double getPosition() {
            return this.position;
        }

        public void setPosition(double position) {
            if (position < 0) {
                throw new IllegalArgumentException("LinearCmd Position cannot be less than 0!");
            }
            if (position > 1) {
                throw new IllegalArgumentException("LinearCmd Position cannot be greater than 1!");
            }

            this.position = position;
        }

        @SuppressWarnings("unused")
        public VectorSubcommand() {
            this.index = -1;
            this.duration = 0;
            this.setPosition(0.0);
        }

        public VectorSubcommand(long index, long duration, double position) {
            this.index = index;
            this.duration = duration;
            this.setPosition(position);
        }
    }

    @JsonProperty(value = "Vectors", required = true)
    @JsonDeserialize(contentUsing = VectorSubcommandDeserializer.class)
    public ArrayList<VectorSubcommand> vectors;

    @SuppressWarnings("unused")
    private LinearCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        this.vectors = new ArrayList<>();
    }

    public LinearCmd(long deviceIndex, ArrayList<VectorSubcommand> vectors) {
        this(deviceIndex, vectors, ButtplugConsts.DefaultMsgId);
    }

    public LinearCmd(long deviceIndex, ArrayList<VectorSubcommand> vectors, long id) {
        super(id, deviceIndex);
        this.vectors = vectors;
    }

}
