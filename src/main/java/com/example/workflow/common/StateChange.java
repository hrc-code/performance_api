package com.example.workflow.common;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StateChange extends JsonDeserializer<Short> {
    @Override
    public Short deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if ("true".equals(value)) {
            return 1;
        } else {
            return 0;
        }
    }
}
