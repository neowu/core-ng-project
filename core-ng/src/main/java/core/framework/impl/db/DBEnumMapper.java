package core.framework.impl.db;

import core.framework.api.db.EnumValue;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author neo
 */
final class DBEnumMapper<T extends Enum> {
    private final Class<? extends Enum> enumClass;
    private final Map<String, Enum<?>> mappings;

    DBEnumMapper(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        mappings = mappings(enumClass);
    }

    @SuppressWarnings("unchecked")
    public T getEnum(String value) {
        if (value == null) return null;
        Enum<?> enumValue = mappings.get(value);
        if (enumValue == null)
            throw Exceptions.error("can not parse value to enum, enumClass={}, value={}", enumClass.getCanonicalName(), value);
        return (T) enumValue;
    }

    private Map<String, Enum<?>> mappings(Class<? extends Enum> enumClass) {
        Enum[] constants = enumClass.getEnumConstants();
        Map<String, Enum<?>> mapping = Maps.newHashMapWithExpectedSize(constants.length);
        for (Enum constant : constants) {
            try {
                Field field = enumClass.getField(constant.name());
                String dbValue = field.getDeclaredAnnotation(EnumValue.class).value();
                mapping.put(dbValue, constant);
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
        return mapping;
    }
}
