package core.framework.impl.db;

import core.framework.api.db.DBEnumValue;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author neo
 */
final class EnumDBMapper {
    private final Map<Class<?>, EnumMap<?, String>> enumToDBValueMappings = Maps.newHashMap();

    <T extends Enum<T>> void registerEnumClass(Class<T> enumClass) {
        if (!enumToDBValueMappings.containsKey(enumClass)) {
            T[] constants = enumClass.getEnumConstants();
            EnumMap<T, String> mappings = new EnumMap<>(enumClass);
            for (T constant : constants) {
                try {
                    Field field = enumClass.getField(constant.name());
                    String dbValue = field.getDeclaredAnnotation(DBEnumValue.class).value();
                    mappings.put(constant, dbValue);
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            }
            enumToDBValueMappings.put(enumClass, mappings);
        }
    }

    String getDBValue(Enum value) {
        Class<? extends Enum> enumClass = value.getClass();
        EnumMap<?, String> mapping = enumToDBValueMappings.get(enumClass);
        if (mapping == null)
            throw Exceptions.error("enum class is not registered, register in module by db().view() or db().repository(), enumClass={}", enumClass.getCanonicalName());
        return mapping.get(value);  // this won't return null since all fields of enum are registered
    }
}
