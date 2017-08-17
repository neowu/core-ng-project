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
    private final Map<Class<? extends Enum<?>>, Map<Enum<?>, String>> mappings = Maps.newHashMap();

    <T extends Enum<?>> void registerEnumClass(Class<T> enumClass) {
        if (!mappings.containsKey(enumClass)) {
            T[] constants = enumClass.getEnumConstants();
            @SuppressWarnings({"unchecked", "rawtypes"})
            Map<Enum<?>, String> mapping = new EnumMap(enumClass);
            for (T constant : constants) {
                try {
                    Field field = enumClass.getField(constant.name());
                    String dbValue = field.getDeclaredAnnotation(DBEnumValue.class).value();
                    mapping.put(constant, dbValue);
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            }
            mappings.put(enumClass, mapping);
        }
    }

    String getDBValue(Enum<?> value) {
        @SuppressWarnings("unchecked")
        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) value.getClass();
        Map<Enum<?>, String> mapping = mappings.get(enumClass);
        if (mapping == null)
            throw Exceptions.error("enum class is not registered, register in module by db().view() or db().repository(), enumClass={}", enumClass.getCanonicalName());
        return mapping.get(value);  // this won't return null since all fields of enum are registered
    }
}
