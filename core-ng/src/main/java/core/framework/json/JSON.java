package core.framework.json;

import core.framework.internal.reflect.GenericTypes;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;

import java.lang.reflect.Type;

import static core.framework.internal.json.JSONMapper.OBJECT_MAPPER;

/**
 * @author neo
 */
public final class JSON {
    public static <T> T fromJSON(Type instanceType, String json) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
        try {
            T result = OBJECT_MAPPER.readValue(json, javaType);
            if (result == null) throw new Error("invalid json value, value=" + json);   // not allow passing "null" as json value
            return result;
        } catch (JacksonException e) {
            // jackson exception contains source info, refer to StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION
            // not leak internal info to external, root cause can be viewed in trace
            throw new JSONException("failed to deserialize json, class=" + GenericTypes.rawClass(instanceType).getCanonicalName(), e);
        }
    }

    public static <T> T fromJSON(Class<T> instanceClass, String json) {
        try {
            T result = OBJECT_MAPPER.readValue(json, instanceClass);
            if (result == null) throw new Error("invalid json value, value=" + json);   // not allow passing "null" as json value
            return result;
        } catch (JacksonException e) {
            // jackson exception contains source info, refer to StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION
            // not leak internal info to external, root cause can be viewed in trace
            throw new JSONException("failed to deserialize json, class=" + instanceClass.getCanonicalName(), e);
        }
    }

    public static String toJSON(Object instance) {
        if (instance == null) throw new Error("instance must not be null");
        return OBJECT_MAPPER.writeValueAsString(instance);
    }

    public static <T extends Enum<?>> T fromEnumValue(Class<T> valueClass, String jsonValue) {
        try {
            return OBJECT_MAPPER.convertValue(jsonValue, valueClass);
        } catch (JacksonException e) {
            throw new JSONException(e);
        }
    }

    public static <T extends Enum<?>> String toEnumValue(T value) {
        return OBJECT_MAPPER.convertValue(value, String.class);
    }
}
