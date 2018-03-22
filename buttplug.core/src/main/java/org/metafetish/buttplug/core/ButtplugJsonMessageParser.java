package org.metafetish.buttplug.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ButtplugJsonMessageParser {

    private ObjectMapper mapper;

    public ButtplugJsonMessageParser() {
        mapper = new ObjectMapper();
        TypeResolverBuilder<?> typer = new DefaultTypeResolverBuilder(
                DefaultTyping.OBJECT_AND_NON_CONCRETE);
        typer = typer.init(JsonTypeInfo.Id.NAME, null);
        typer = typer.inclusion(As.WRAPPER_OBJECT);
        mapper.setDefaultTyping(typer);
    }

    public List<ButtplugMessage> deserialize(String json)
            throws IOException {
        return Arrays.asList(mapper.readValue(json, ButtplugMessage[].class));
    }

    public String serialize(List<ButtplugMessage> msgs, long clientSchemaVersion)
            throws IOException {
        //TODO: Support downgrading messages
        return mapper.writeValueAsString(msgs);
    }

    public String serialize(final ButtplugMessage msg, long clientSchemaVersion)
            throws IOException {
        return this.serialize(new ArrayList<ButtplugMessage>(){{ add(msg); }}, clientSchemaVersion);
    }
}
