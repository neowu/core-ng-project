package core.framework.impl.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

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
    public static final ObjectMapper OBJECT_MAPPER = createMapper();

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new AfterburnerModule());
        mapper.setDateFormat(new ISO8601DateFormat());
        mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);
        return mapper;
    }

    public static <T> T fromJSONValue(Class<T> valueType, String jsonValue) {
        return OBJECT_MAPPER.convertValue(jsonValue, valueType);
    }

    public static String toJSONValue(Object value) {
        return OBJECT_MAPPER.convertValue(value, String.class);
    }

    public static <T> T fromMapValue(Type instanceType, Map<String, String> map) {
        ObjectMapper objectMapper = OBJECT_MAPPER;
        JavaType type = objectMapper.getTypeFactory().constructType(instanceType);
        try {
            byte[] json = objectMapper.writeValueAsBytes(map);
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, String> toMapValue(Object instance) {
        ObjectMapper objectMapper = OBJECT_MAPPER;
        MapType type = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
        try {
            byte[] json = objectMapper.writeValueAsBytes(instance);
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // jackson detects encoding and default to utf-8, works with our scenario, so there not to specify charset
    public static <T> T fromJSON(Type instanceType, byte[] json) {
        ObjectMapper objectMapper = OBJECT_MAPPER;
        JavaType type = objectMapper.getTypeFactory().constructType(instanceType);
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] toJSON(Object instance) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(instance);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
