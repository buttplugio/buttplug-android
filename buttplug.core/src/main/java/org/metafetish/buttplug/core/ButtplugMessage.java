package org.metafetish.buttplug.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import org.metafetish.buttplug.core.Messages.DeviceAdded;
import org.metafetish.buttplug.core.Messages.DeviceList;
import org.metafetish.buttplug.core.Messages.DeviceMessageInfo;
import org.metafetish.buttplug.core.Messages.DeviceRemoved;
import org.metafetish.buttplug.core.Messages.Error;
import org.metafetish.buttplug.core.Messages.FleshlightLaunchFW12Cmd;
import org.metafetish.buttplug.core.Messages.KiirooCmd;
import org.metafetish.buttplug.core.Messages.LinearCmd;
import org.metafetish.buttplug.core.Messages.Log;
import org.metafetish.buttplug.core.Messages.LovenseCmd;
import org.metafetish.buttplug.core.Messages.MessageAttributes;
import org.metafetish.buttplug.core.Messages.Ok;
import org.metafetish.buttplug.core.Messages.Ping;
import org.metafetish.buttplug.core.Messages.RequestDeviceList;
import org.metafetish.buttplug.core.Messages.RequestLog;
import org.metafetish.buttplug.core.Messages.RequestServerInfo;
import org.metafetish.buttplug.core.Messages.RotateCmd;
import org.metafetish.buttplug.core.Messages.ScanningFinished;
import org.metafetish.buttplug.core.Messages.ServerInfo;
import org.metafetish.buttplug.core.Messages.SingleMotorVibrateCmd;
import org.metafetish.buttplug.core.Messages.StartScanning;
import org.metafetish.buttplug.core.Messages.StopAllDevices;
import org.metafetish.buttplug.core.Messages.StopDeviceCmd;
import org.metafetish.buttplug.core.Messages.StopScanning;
import org.metafetish.buttplug.core.Messages.Test;
import org.metafetish.buttplug.core.Messages.VibrateCmd;
import org.metafetish.buttplug.core.Messages.VorzeA10CycloneCmd;

@JsonTypeInfo(include = As.WRAPPER_OBJECT, use = Id.NAME)
//TODO: Add Version0 messages
@JsonSubTypes({
        @JsonSubTypes.Type(value = Ok.class, name = "Ok"),
        @JsonSubTypes.Type(value = Ping.class, name = "Ping"),
        @JsonSubTypes.Type(value = Test.class, name = "Test"),
        @JsonSubTypes.Type(value = Error.class, name = "Error"),
        @JsonSubTypes.Type(value = MessageAttributes.class, name = "MessageAttributes"),
        @JsonSubTypes.Type(value = DeviceMessageInfo.class, name = "DeviceMessageInfo"),
        @JsonSubTypes.Type(value = DeviceList.class, name = "DeviceList"),
        @JsonSubTypes.Type(value = DeviceAdded.class, name = "DeviceAdded"),
        @JsonSubTypes.Type(value = DeviceRemoved.class, name = "DeviceRemoved"),
        @JsonSubTypes.Type(value = RequestDeviceList.class, name = "RequestDeviceList"),
        @JsonSubTypes.Type(value = StartScanning.class, name = "StartScanning"),
        @JsonSubTypes.Type(value = StopScanning.class, name = "StopScanning"),
        @JsonSubTypes.Type(value = ScanningFinished.class, name = "ScanningFinished"),
        @JsonSubTypes.Type(value = RequestLog.class, name = "RequestLog"),
        @JsonSubTypes.Type(value = Log.class, name = "Log"),
        @JsonSubTypes.Type(value = RequestServerInfo.class, name = "RequestServerInfo"),
        @JsonSubTypes.Type(value = ServerInfo.class, name = "ServerInfo"),
        @JsonSubTypes.Type(value = FleshlightLaunchFW12Cmd.class, name = "FleshlightLaunchFW12Cmd"),
        @JsonSubTypes.Type(value = LovenseCmd.class, name = "LovenseCmd"),
        @JsonSubTypes.Type(value = KiirooCmd.class, name = "KiirooCmd"),
        @JsonSubTypes.Type(value = VorzeA10CycloneCmd.class, name = "VorzeA10CycloneCmd"),
        @JsonSubTypes.Type(value = SingleMotorVibrateCmd.class, name = "SingleMotorVibrateCmd"),
        @JsonSubTypes.Type(value = VibrateCmd.class, name = "VibrateCmd"),
        @JsonSubTypes.Type(value = RotateCmd.class, name = "RotateCmd"),
        @JsonSubTypes.Type(value = LinearCmd.class, name = "LinearCmd"),
        @JsonSubTypes.Type(value = StopDeviceCmd.class, name = "StopDeviceCmd"),
        @JsonSubTypes.Type(value = StopAllDevices.class, name = "StopAllDevices")
})
public abstract class ButtplugMessage {

    @JsonIgnore
    protected final long currentSchemaVersion = 1;

    @JsonProperty(value = "Id", required = true)
    public long id;

    public ButtplugMessage(long id) {
        this.id = id;
    }
}
