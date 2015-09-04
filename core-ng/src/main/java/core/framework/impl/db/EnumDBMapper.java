package core.framework.impl.db;

import core.framework.api.db.EnumValue;
import core.framework.api.util.Maps;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author neo
 */
public class EnumDBMapper {
    private final Map<Class<? extends Enum>, EnumMap<?, String>> enumToDBValueMappings = Maps.newConcurrentHashMap();

    void registerEnumClass(Class<? extends Enum> enumClass) {
        enumToDBValueMappings.computeIfAbsent(enumClass, this::enumToDBValueMapping);
    }

    String getDBValue(Enum enumValue) {
        EnumMap<?, String> mapping = enumToDBValueMappings.computeIfAbsent(enumValue.getClass(), this::enumToDBValueMapping);
        return mapping.get(enumValue);
    }

    @SuppressWarnings("unchecked")
    private EnumMap<?, String> enumToDBValueMapping(Class<? extends Enum> enumClass) {
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
