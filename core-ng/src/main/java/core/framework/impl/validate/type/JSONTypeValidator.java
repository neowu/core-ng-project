package core.framework.impl.validate.type;

import core.framework.api.json.Property;
import core.framework.impl.reflect.Enums;
import core.framework.impl.reflect.Fields;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.Sets;
import core.framework.util.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class JSONTypeValidator implements TypeVisitor {
    public static void validateEnumClass(Class<?> enumClass) {
        Enum<?>[] constants = (Enum<?>[]) enumClass.getEnumConstants();
        for (Enum<?> constant : constants) {
            Property property = Enums.constantAnnotation(constant, Property.class);
            if (property == null) {
                throw Exceptions.error("enum must have @Property, enum={}", Enums.path(constant));
            }
        }
    }

    protected final DataTypeValidator validator;
    private final Map<String, Set<String>> properties = Maps.newHashMap();

    protected JSONTypeValidator(Type instanceType) {
        validator = new DataTypeValidator(instanceType);
        validator.allowedValueClass = this::allowedValueClass;
        validator.allowChild = true;
        validator.visitor = this;
    }

    public void validate() {
        validator.validate();
    }

    private boolean allowedValueClass(Class<?> valueClass) {
        return String.class.equals(valueClass)
                || Integer.class.equals(valueClass)
                || Boolean.class.equals(valueClass)
                || Long.class.equals(valueClass)
                || Double.class.equals(valueClass)
                || BigDecimal.class.equals(valueClass)
                || LocalDate.class.equals(valueClass)
                || LocalDateTime.class.equals(valueClass)
                || ZonedDateTime.class.equals(valueClass)
                || Instant.class.equals(valueClass)
                || valueClass.isEnum();
    }

    @Override
    public void visitField(Field field, String parentPath) {
        Property property = field.getDeclaredAnnotation(Property.class);
        if (property == null)
            throw Exceptions.error("field must have @Property, field={}", Fields.path(field));

        String name = property.name();

        if (Strings.isEmpty(name)) {
            throw Exceptions.error("@Property name attribute must not be empty, field={}", Fields.path(field));
        }

        Set<String> properties = this.properties.computeIfAbsent(parentPath, key -> Sets.newHashSet());
        if (properties.contains(name)) {
            throw Exceptions.error("found duplicate property, field={}, name={}", Fields.path(field), name);
        }
        properties.add(name);

        Class<?> fieldClass = field.getType();
        if (fieldClass.isEnum()) {
            validateEnumClass(fieldClass);
        }
    }
}
