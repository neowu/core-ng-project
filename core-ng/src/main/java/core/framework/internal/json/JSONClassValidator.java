package core.framework.internal.json;

import core.framework.api.json.Property;
import core.framework.internal.reflect.Classes;
import core.framework.internal.reflect.Fields;
import core.framework.internal.validate.ClassValidator;
import core.framework.internal.validate.ClassVisitor;
import core.framework.util.Maps;
import core.framework.util.Sets;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class JSONClassValidator implements ClassVisitor {
    public static void validateEnum(Class<?> enumClass) {
        Set<String> enumValues = Sets.newHashSet();
        List<Field> fields = Classes.enumConstantFields(enumClass);
        for (Field field : fields) {
            Property property = field.getDeclaredAnnotation(Property.class);
            if (property == null)
                throw new Error("enum must have @Property, field=" + Fields.path(field));
            boolean added = enumValues.add(property.name());
            if (!added)
                throw new Error(format("found duplicate property, field={}, name={}", Fields.path(field), property.name()));
        }
    }

    private final ClassValidator validator;
    private final Map<String, Set<String>> visitedProperties = Maps.newHashMap();

    public JSONClassValidator(Class<?> instanceClass) {
        validator = new ClassValidator(instanceClass);
        validator.allowedValueClasses = Set.of(String.class, Boolean.class,
            Integer.class, Long.class, Double.class, BigDecimal.class,
            LocalDate.class, LocalDateTime.class, ZonedDateTime.class, Instant.class, LocalTime.class);
        validator.visitor = this;
    }

    public void validate() {
        validator.validate();
    }

    @Override
    public void visitField(Field field, String parentPath) {
        Property property = field.getDeclaredAnnotation(Property.class);
        if (property == null)
            throw new Error("field must have @Property, field=" + Fields.path(field));

        String name = property.name();
        if (name.isBlank()) throw new Error("@Property name attribute must not be blank, field=" + Fields.path(field));

        boolean added = visitedProperties.computeIfAbsent(parentPath, key -> Sets.newHashSet()).add(name);
        if (!added) {
            throw new Error(format("found duplicate property, field={}, name={}", Fields.path(field), name));
        }
    }

    @Override
    public void visitEnum(Class<?> enumClass) {
        validateEnum(enumClass);
    }
}
