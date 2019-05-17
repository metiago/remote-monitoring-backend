package io.tiago.monitor.helper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;

public class JsonHelper extends Json {

    // Overridden method to serialize Java 8 Localtime
    public static String encodePrettily(Object obj) {
        try {
            prettyMapper.registerModule(new JavaTimeModule());
            return prettyMapper.writeValueAsString(obj);
        } catch (Exception var2) {
            throw new EncodeException("Failed to encode as JSON: " + var2.getMessage());
        }
    }
}
