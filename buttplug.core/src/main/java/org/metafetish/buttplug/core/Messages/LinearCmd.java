package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

import java.util.List;

public class LinearCmd extends ButtplugDeviceMessage {
    public class VectorSubcommands {
        private double positionImpl;

        @JsonProperty(required = true)
        public long index;

        @JsonProperty(required = true)
        public long duration;

        //        @JsonProperty(required = true)
        public double getPosition() {
            return this.positionImpl;
        }

        public void setPosition(double aPosition) {
            if (aPosition < 0) {
                throw new IllegalArgumentException("LinearCmd Position cannot be less than 0!");
            }
            if (aPosition > 1) {
                throw new IllegalArgumentException("LinearCmd Position cannot be greater than 1!");
            }

            this.positionImpl = aPosition;
        }

        public VectorSubcommands(long aIndex, long aDuration, double aPosition) {
            this.index = aIndex;
            this.duration = aDuration;
            this.setPosition(aPosition);
        }
    }

    @JsonProperty(required = true)
    public List<VectorSubcommands> vectors;

    public LinearCmd(long aDeviceIndex, List<VectorSubcommands> aVectors) {
        this(aDeviceIndex, aVectors, ButtplugConsts.DefaultMsgId);
    }

    public LinearCmd(long aDeviceIndex, List<VectorSubcommands> aVectors, long aId) {
        super(aId, aDeviceIndex, 1);
        this.vectors = aVectors;
    }

}
