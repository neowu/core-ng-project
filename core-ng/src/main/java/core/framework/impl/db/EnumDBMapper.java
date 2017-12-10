package core.framework.impl.db;

import core.framework.db.DBEnumValue;
import core.framework.impl.reflect.Enums;
import core.framework.util.Exceptions;
import core.framework.util.Maps;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author neo
 */
final class EnumDBMapper {
    private final Map<Class<? extends Enum<?>>, Map<Enum<?>, String>> mappings = Maps.newHashMap();

    <T extends Enum<?>> void registerEnumClass(Class<T> enumClass) {
        if (!mappings.containsKey(enumClass)) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Map<Enum<?>, String> mapping = new EnumMap(enumClass);
            T[] constants = enumClass.getEnumConstants();
            for (T constant : constants) {
                String dbValue = Enums.constantAnnotation(constant, DBEnumValue.class).value();
                mapping.put(constant, dbValue);
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
