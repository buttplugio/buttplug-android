package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.List;

public class LinearCmd extends ButtplugDeviceMessage {
    public class VectorSubcommands {
        private double position;

        @JsonProperty(required = true)
        public long index;

        @JsonProperty(required = true)
        public long duration;

        //@JsonProperty(required = true)
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

        public VectorSubcommands(long index, long duration, double position) {
            this.index = index;
            this.duration = duration;
            this.setPosition(position);
        }
    }

    @JsonProperty(required = true)
    public List<VectorSubcommands> vectors;

    public LinearCmd(long deviceIndex, List<VectorSubcommands> vectors) {
        this(deviceIndex, vectors, ButtplugConsts.DefaultMsgId);
    }

    public LinearCmd(long deviceIndex, List<VectorSubcommands> vectors, long id) {
        super(id, deviceIndex);
        this.vectors = vectors;
    }

}
