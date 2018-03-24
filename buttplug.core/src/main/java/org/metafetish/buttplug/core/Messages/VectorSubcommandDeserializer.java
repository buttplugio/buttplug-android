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
public class VectorSubcommandDeserializer extends StdDeserializer<LinearCmd.VectorSubcommand>
        implements ResolvableDeserializer {
    private JsonDeserializer<Object> underlyingDeserializer;

    public VectorSubcommandDeserializer() {
        super(LinearCmd.VectorSubcommand.class);
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        underlyingDeserializer = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructType(LinearCmd.VectorSubcommand.class));
    }

    @Override
    public LinearCmd.VectorSubcommand deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonStreamContext ourContext = p.getParsingContext();
        JsonStreamContext listContext = ourContext.getParent();
        JsonStreamContext containerContext = listContext.getParent();
        LinearCmd container = (LinearCmd) containerContext.getCurrentValue();
        LinearCmd.VectorSubcommand value = container.new VectorSubcommand();
        underlyingDeserializer.deserialize(p, ctxt, value);
        return value;
    }
}
