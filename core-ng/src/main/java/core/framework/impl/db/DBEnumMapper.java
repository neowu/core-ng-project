package core.framework.impl.db;

import core.framework.api.db.DBEnumValue;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author neo
 */
final class DBEnumMapper<T extends Enum<T>> {
    private final Class<T> enumClass;
    private final Map<String, Enum<T>> mappings;

    DBEnumMapper(Class<T> enumClass) {
        this.enumClass = enumClass;
        mappings = mappings(enumClass);
    }

    public T getEnum(String value) {
        if (value == null) return null;
        @SuppressWarnings("unchecked")
        T enumValue = (T) mappings.get(value);
        if (enumValue == null)
            throw Exceptions.error("can not parse value to enum, enumClass={}, value={}", enumClass.getCanonicalName(), value);
        return enumValue;
    }

    private Map<String, Enum<T>> mappings(Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        Map<String, Enum<T>> mapping = Maps.newHashMapWithExpectedSize(constants.length);
        for (T constant : constants) {
            try {
                Field field = enumClass.getField(constant.name());
                String dbValue = field.getDeclaredAnnotation(DBEnumValue.class).value();
                mapping.put(dbValue, constant);
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
        return mapping;
    }
}
