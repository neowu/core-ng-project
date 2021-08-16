package core.framework.internal.web.api;

import core.framework.api.json.Property;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotBlank;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import core.framework.api.validate.Size;
import core.framework.api.web.service.QueryParam;
import core.framework.internal.reflect.Classes;
import core.framework.internal.reflect.GenericTypes;
import core.framework.util.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
class APITypeParser {
    private final Map<String, APIType> types = Maps.newLinkedHashMap();
    private final Set<Class<?>> valueClasses = Set.of(String.class, Boolean.class,
        Integer.class, Long.class, Double.class, BigDecimal.class,
        LocalDate.class, LocalDateTime.class, ZonedDateTime.class, LocalTime.class);

    List<APIType> types() {
        return new ArrayList<>(types.values());
    }

    TypeDefinition parseType(Type type) {
        if (type == void.class) return new TypeDefinition("void");
        if (GenericTypes.isList(type)) {
            var valueType = parseType(GenericTypes.listValueClass(type));
            return new TypeDefinition("List").addParam(valueType.type);
        }
        if (GenericTypes.isMap(type)) {
            Class<?> keyClass = GenericTypes.mapKeyClass(type);
            var definition = new TypeDefinition("Map");
            if (String.class.equals(keyClass)) {
                definition.addParam("String");
            } else {        // Map key can only be String or enum
                definition.addParam(parseEnumType(keyClass));
            }
            var valueType = parseType(GenericTypes.mapValueType(type));
            definition.addParam(valueType.type);
            if (valueType.params != null) definition.addParam(valueType.params.get(0));    // Map<T, List<V>> is most complex type
            return definition;
        }
        Class<?> rawClass = GenericTypes.rawClass(type);  // it must be class type at this point
        if (valueClasses.contains(rawClass)) return new TypeDefinition(rawClass.getSimpleName());
        if (Instant.class.equals(type)) return new TypeDefinition("ZonedDateTime");
        if (rawClass.isEnum()) {
            return new TypeDefinition(parseEnumType((Class<?>) type));
        }
        return new TypeDefinition(parseBeanType(rawClass));
    }

    String parseBeanType(Class<?> beanClass) {
        String className = Classes.className(beanClass);
        if (!types.containsKey(className)) {
            List<Field> fields = Classes.instanceFields(beanClass);
            var definition = new APIType();
            definition.type = "bean";
            definition.name = className;
            definition.fields = new ArrayList<>(fields.size());
            types.put(className, definition);  // put into map before parsing fields to handle circular reference
            for (Field field : fields) {
                var fieldDefinition = new APIType.Field();
                fieldDefinition.name = fieldName(field);
                var type = parseType(field.getGenericType());
                fieldDefinition.type = type.type;
                fieldDefinition.typeParams = type.params;
                parseConstraints(field, fieldDefinition.constraints);
                definition.fields.add(fieldDefinition);
            }
        }
        return className;
    }

    String parseEnumType(Class<?> enumClass) {
        String className = Classes.className(enumClass);
        types.computeIfAbsent(className, key -> {
            List<Field> fields = Classes.enumConstantFields(enumClass);
            var definition = new APIType();
            definition.type = "enum";
            definition.name = className;
            definition.enumConstants = new ArrayList<>(fields.size());
            for (Field field : fields) {
                var constant = new APIType.EnumConstant();
                constant.name = field.getName();
                constant.value = field.getDeclaredAnnotation(Property.class).name();
                definition.enumConstants.add(constant);
            }
            return definition;
        });
        return className;
    }

    private void parseConstraints(Field field, APIType.Constraints constraints) {
        constraints.notNull = field.isAnnotationPresent(NotNull.class);
        constraints.notBlank = field.isAnnotationPresent(NotBlank.class);
        Min min = field.getDeclaredAnnotation(Min.class);
        if (min != null) constraints.min = min.value();
        Max max = field.getDeclaredAnnotation(Max.class);
        if (max != null) constraints.max = max.value();
        Size size = field.getDeclaredAnnotation(Size.class);
        if (size != null) {
            constraints.size = new APIType.Size();
            constraints.size.min = size.min();
            constraints.size.max = size.max();
        }
        Pattern pattern = field.getDeclaredAnnotation(Pattern.class);
        if (pattern != null) constraints.pattern = pattern.value();
    }

    private String fieldName(Field field) {
        Property property = field.getDeclaredAnnotation(Property.class);
        if (property != null) return property.name();
        return field.getDeclaredAnnotation(QueryParam.class).name();
    }

    static class TypeDefinition {
        final String type;
        List<String> params;

        TypeDefinition(String type) {
            this.type = type;
        }

        TypeDefinition addParam(String param) {
            if (params == null) params = new ArrayList<>();
            params.add(param);
            return this;
        }
    }
}
