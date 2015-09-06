package core.framework.impl.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class TypeInspector {
    public final Type type;
    public final Class<?> rawClass;

    public TypeInspector(Type type) {
        if (type == null) throw new Error("type must not be null");
        this.type = type;
        rawClass = (Class) ((type instanceof Class) ? type : ((ParameterizedType) type).getRawType());
    }

    public boolean isList() {
        return List.class.isAssignableFrom(rawClass);
    }

    public boolean isGenericList() {
        if (!List.class.equals(rawClass)) return false;
        if (!(type instanceof ParameterizedType)) return false;
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[0];
        if (!(valueType instanceof Class)) return false;
        return true;
    }

    public Class<?> listValueClass() {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(rawClass);
    }

    public boolean isGenericStringMap() {
        if (!Map.class.equals(rawClass)) return false;
        if (!(type instanceof ParameterizedType)) return false;
        Type keyType = ((ParameterizedType) type).getActualTypeArguments()[0];
        if (!(keyType instanceof Class)) return false;
        Class keyClass = (Class) keyType;
        if (!String.class.equals(keyClass)) return false;
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];
        if (!(valueType instanceof Class)) return false;
        return true;
    }

    public Class<?> mapValueClass() {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[1];
    }
}
