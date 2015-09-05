package core.framework.impl.db;

import core.framework.api.db.EnumValue;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author neo
 */
public final class EnumDBMapper {
    private final Map<Class<? extends Enum>, EnumMap<?, String>> enumToDBValueMappings = Maps.newHashMap();

    void registerEnumClass(Class<? extends Enum> enumClass) {
        enumToDBValueMappings.computeIfAbsent(enumClass, this::mappings);
    }

    String getDBValue(Enum value) {
        Class<? extends Enum> enumClass = value.getClass();
        EnumMap<?, String> mapping = enumToDBValueMappings.get(enumClass);
        if (mapping == null)
            throw Exceptions.error("enum class is not registered, register in module by db().view() or db().repository(), enumClass={}",
                enumClass.getCanonicalName());
        return mapping.get(value);
    }

    @SuppressWarnings("unchecked")
    private EnumMap<?, String> mappings(Class<? extends Enum> enumClass) {
        Enum[] constants = enumClass.getEnumConstants();
        EnumMap mapping = new EnumMap<>(enumClass);
        for (Enum constant : constants) {
            try {
                Field field = enumClass.getField(constant.name());
                String dbValue = field.getDeclaredAnnotation(EnumValue.class).value();
                mapping.put(constant, dbValue);
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
        return mapping;
    }
}
