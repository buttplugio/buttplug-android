package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;

import java.io.IOException;

//https://stackoverflow.com/a/33712110
public class VibrateSubcommandDeserializer extends StdDeserializer<VibrateCmd.VibrateSubcommand>
        implements ResolvableDeserializer {
    private JsonDeserializer<Object> underlyingDeserializer;

    public VibrateSubcommandDeserializer() {
        super(VibrateCmd.VibrateSubcommand.class);
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        underlyingDeserializer = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructType(VibrateCmd.VibrateSubcommand.class));
    }

    @Override
    public VibrateCmd.VibrateSubcommand deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonStreamContext ourContext = p.getParsingContext();
        JsonStreamContext listContext = ourContext.getParent();
        JsonStreamContext containerContext = listContext.getParent();
        VibrateCmd container = (VibrateCmd) containerContext.getCurrentValue();
        VibrateCmd.VibrateSubcommand value = container.new VibrateSubcommand();
        underlyingDeserializer.deserialize(p, ctxt, value);
        return value;
    }
}
