package com.netapp.test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.netapp.test.data.JsonObject;
import com.netapp.test.data.Product;

import java.io.IOException;

public class CustomDeserializer extends StdDeserializer<JsonObject> {

    public CustomDeserializer() {
        this(null);
    }

    public CustomDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public JsonObject deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        var beforeNode = node.get("before");
        Product before = null;
        if (beforeNode != null && !beforeNode.isNull()) {
            int beforeId = (Integer) beforeNode.get("id").numberValue();
            String beforeName = beforeNode.get("name").asText();
            String beforeDescription = beforeNode.get("description").asText();
            double beforeWeight = beforeNode.get("weight").asDouble();
            before = new Product(beforeId, beforeName, beforeDescription, beforeWeight);
        }

        var afterNode = node.get("after");
        Product after = null;
        if (afterNode != null) {
            int afterId = (Integer) (afterNode.get("id")).numberValue();
            String afterName = afterNode.get("name").asText();
            String afterDescription = afterNode.get("description").asText();
            double afterWeight = afterNode.get("weight").asDouble();
            after = new Product(afterId, afterName, afterDescription, afterWeight);
        }
        return new JsonObject(before, after);
    }
}
