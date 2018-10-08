package core.framework.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import core.framework.impl.json.JSONAnnotationIntrospector;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

/**
 * @author neo
 */
public final class JSON {
    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new AfterburnerModule().setUseValueClassLoader(false));   // disable value class loader to avoid jdk illegal reflection warning, requires JSON class/fields must be public
        mapper.setDateFormat(new StdDateFormat());
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setAnnotationIntrospector(new JSONAnnotationIntrospector());
        return mapper;
    }

    public static Object fromJSON(Type instanceType, String json) {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T fromJSON(Class<T> instanceClass, String json) {
        try {
            return OBJECT_MAPPER.readValue(json, instanceClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toJSON(Object instance) {
        try {
            return OBJECT_MAPPER.writeValueAsString(instance);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T extends Enum<?>> T fromEnumValue(Class<T> valueClass, String jsonValue) {
        return OBJECT_MAPPER.convertValue(jsonValue, valueClass);
    }

    public static <T extends Enum<?>> String toEnumValue(T value) {
        return OBJECT_MAPPER.convertValue(value, String.class);
    }
}
