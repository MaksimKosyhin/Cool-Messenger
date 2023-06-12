package com.example.end.config.json.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.mongodb.DBRef;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DBRefDeserializer extends JsonDeserializer<DBRef> {
    @Override
    public DBRef deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Map<String, String> fields = p.readValueAs(HashMap.class);
        DBRef dbRef = new DBRef(fields.get("collectionName"), fields.get("id"));
        return dbRef;
    }
}
