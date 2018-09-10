package core.framework.impl.db;

import core.framework.db.DBEnumValue;
import core.framework.impl.reflect.Enums;
import core.framework.util.Maps;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
final class DBEnumMapper<T extends Enum<T>> {
    private final Class<T> enumClass;
    private final Map<String, T> mappings;

    DBEnumMapper(Class<T> enumClass) {
        this.enumClass = enumClass;
        mappings = mappings(enumClass);
    }

    // used by generated code, must be public
    public T getEnum(String value) {
        if (value == null) return null;
        T enumValue = mappings.get(value);
        if (enumValue == null)
            throw new Error(format("can not parse value to enum, enumClass={}, value={}", enumClass.getCanonicalName(), value));
        return enumValue;
    }

    private Map<String, T> mappings(Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        Map<String, T> mapping = Maps.newHashMapWithExpectedSize(constants.length);
        for (T constant : constants) {
            String dbValue = Enums.constantAnnotation(constant, DBEnumValue.class).value();
            mapping.put(dbValue, constant);
        }
        return mapping;
    }
}
