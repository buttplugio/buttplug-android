package org.metafetish.buttplug.core.Messages;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

//https://stackoverflow.com/a/33712110
public class RotateSubcommandDeserializer extends StdDeserializer<RotateCmd.RotateSubcommand>
        implements ResolvableDeserializer {
    private JsonDeserializer<Object> underlyingDeserializer;

    public RotateSubcommandDeserializer() {
        super(RotateCmd.RotateSubcommand.class);
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        underlyingDeserializer = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructType(RotateCmd.RotateSubcommand.class));
    }

    @Override
    public RotateCmd.RotateSubcommand deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonStreamContext ourContext = p.getParsingContext();
        JsonStreamContext listContext = ourContext.getParent();
        JsonStreamContext containerContext = listContext.getParent();
        RotateCmd container = (RotateCmd) containerContext.getCurrentValue();
        RotateCmd.RotateSubcommand value = container.new RotateSubcommand();
        underlyingDeserializer.deserialize(p, ctxt, value);
        return value;
    }
}
