package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.metafetish.buttplug.core.ButtplugConsts;
import org.metafetish.buttplug.core.ButtplugDeviceMessage;

@JsonPropertyOrder({"Id", "DeviceIndex", "Command"})
public class KiirooCmd extends ButtplugDeviceMessage {

    @JsonProperty(value = "Command", required = true)
    public String deviceCmd;

    @JsonIgnore
    private int position;

    public int getPosition() {
        if (position > 4 || position < 0) {
            return 0;
        }
        return position;
    }

    public void setPosition(int position) {
        if (position > 4) {
            throw new IllegalArgumentException(
                    "KiirooRawCmd cannot have a position higher than 4!");
        }

        if (position < 0) {
            throw new IllegalArgumentException(
                    "KiirooRawCmd cannot have a position lower than 0!");
        }

        this.position = position;
    }

    public KiirooCmd(long deviceIndex, int position) {
        this(deviceIndex, position, ButtplugConsts.DefaultMsgId);
    }

    public KiirooCmd(long deviceIndex, int position, long id) {
        super(id, deviceIndex);
        this.setPosition(position);
    }

    @SuppressWarnings("unused")
    private KiirooCmd() {
        super(ButtplugConsts.DefaultMsgId, -1);
        this.deviceCmd = "";
    }
}
