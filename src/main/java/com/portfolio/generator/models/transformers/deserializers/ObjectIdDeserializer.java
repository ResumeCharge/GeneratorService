package com.portfolio.generator.models.transformers.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class ObjectIdDeserializer extends StdDeserializer<String> {

  public ObjectIdDeserializer() {
    super(String.class);
  }

  @Override
  public String deserialize(final JsonParser jsonParser,
                            final DeserializationContext deserializationContext) throws IOException {
    final JsonNode oid = ((JsonNode) jsonParser.readValueAsTree()).get("$oid");
    return oid.textValue();
  }
}