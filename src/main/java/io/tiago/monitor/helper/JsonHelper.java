package io.tiago.monitor.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;

import java.io.IOException;

/**
 * JsonHelper is responsible to centralize serialization methods as we as to support jdk8 api eg. Localtime
 */
public class JsonHelper extends Json {

    public static String encodePrettily(Object obj) {
        try {
            prettyMapper.registerModule(new JavaTimeModule());
            return prettyMapper.writeValueAsString(obj);
        } catch (Exception var2) {
            throw new EncodeException("Failed to encode as JSON: " + var2.getMessage());
        }
    }

    public static Object readValue(String obj, Class clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
        return mapper.readValue(obj, clazz);
    }
}
