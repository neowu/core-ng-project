package core.framework.impl.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import core.framework.api.util.JSON;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * used internally, performance is top priority in design
 *
 * @author neo
 */
public final class JSONMapper {
    public static <T> T fromJSONValue(Class<T> valueType, String jsonValue) {
        return JSON.OBJECT_MAPPER.convertValue(jsonValue, valueType);
    }

    public static String toJSONValue(Object value) {
        return JSON.OBJECT_MAPPER.convertValue(value, String.class);
    }

    public static <T> T fromMapValue(Type instanceType, Map<String, String> map) {
        ObjectMapper objectMapper = JSON.OBJECT_MAPPER;
        JavaType type = objectMapper.getTypeFactory().constructType(instanceType);
        try {
            byte[] json = objectMapper.writeValueAsBytes(map);
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, String> toMapValue(Object instance) {
        ObjectMapper objectMapper = JSON.OBJECT_MAPPER;
        MapType type = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
        try {
            byte[] json = objectMapper.writeValueAsBytes(instance);
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T fromJSON(Class<T> instanceType, byte[] json) {
        try {
            return JSON.OBJECT_MAPPER.readValue(json, instanceType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] toJSON(Object instance) {
        try {
            return JSON.OBJECT_MAPPER.writeValueAsBytes(instance);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
