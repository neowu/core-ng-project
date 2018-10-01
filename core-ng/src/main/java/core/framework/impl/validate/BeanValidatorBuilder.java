package core.framework.impl.validate;

import core.framework.api.validate.Length;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotBlank;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import core.framework.api.validate.Size;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.Fields;
import core.framework.impl.reflect.GenericTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;

import static core.framework.impl.asm.Literal.type;
import static core.framework.impl.asm.Literal.variable;
import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class BeanValidatorBuilder {
    private final Class<?> beanClass;
    private final Function<Field, String> fieldNameProvider;
    DynamicInstanceBuilder<BeanValidator> builder;
    private int index = 0;

    public BeanValidatorBuilder(Class<?> beanClass, Function<Field, String> fieldNameProvider) {
        this.beanClass = beanClass;
        this.fieldNameProvider = fieldNameProvider;
    }

    public Optional<BeanValidator> build() {
        validate(beanClass);
        if (Classes.instanceFields(beanClass).stream().noneMatch(this::hasValidationAnnotation)) return Optional.empty();
        builder = new DynamicInstanceBuilder<>(BeanValidator.class, beanClass.getName() + "$Validator");
        String method = validateMethod(beanClass, null);
        var builder = new CodeBuilder().append("public void validate(Object instance, {} errors, boolean partial) {\n", type(ValidationErrors.class));
        builder.indent(1).append("{}(({}) instance, errors, partial);\n", method, type(beanClass));
        builder.append('}');
        this.builder.addMethod(builder.build());
        return Optional.of(this.builder.build());
    }

    private String validateMethod(Class<?> beanClass, String parentPath) {
        String methodName = "validate" + beanClass.getSimpleName() + (index++);
        var builder = new CodeBuilder().append("private void {}({} bean, {} errors, boolean partial) {\n", methodName, type(beanClass), type(ValidationErrors.class));
        for (Field field : Classes.instanceFields(beanClass)) {
            if (!hasValidationAnnotation(field)) continue;

            Type fieldType = field.getGenericType();
            Class<?> fieldClass = GenericTypes.rawClass(fieldType);
            String pathLiteral = variable(path(field, parentPath));

            builder.indent(1).append("if (bean.{} == null) {\n", field.getName());
            NotNull notNull = field.getDeclaredAnnotation(NotNull.class);
            if (notNull != null)
                builder.indent(2).append("if (!partial) errors.add({}, {});\n", pathLiteral, variable(notNull.message()));
            builder.indent(1).append("} else {\n");

            if (String.class.equals(fieldClass)) {
                buildStringValidation(builder, field, pathLiteral);
            } else if (GenericTypes.isList(fieldType)) {
                buildListValidation(builder, field, pathLiteral, parentPath);
            } else if (GenericTypes.isMap(fieldType)) {
                buildMapValidation(builder, field, pathLiteral, parentPath);
            } else if (Number.class.isAssignableFrom(fieldClass)) {
                buildNumberValidation(builder, field, pathLiteral);
            } else if (!isValueClass(fieldClass)) {
                String method = validateMethod(fieldClass, path(field, parentPath));
                builder.indent(2).append("{}(bean.{}, errors, partial);\n", method, field.getName());
            }

            builder.indent(1).append("}\n");
        }
        builder.append('}');
        this.builder.addMethod(builder.build());
        return methodName;
    }

    private void buildNumberValidation(CodeBuilder builder, Field field, String pathLiteral) {
        Min min = field.getDeclaredAnnotation(Min.class);
        if (min != null) builder.indent(2).append("if (bean.{}.doubleValue() < {}) errors.add({}, {});\n", field.getName(), min.value(), pathLiteral, variable(min.message()));
        Max max = field.getDeclaredAnnotation(Max.class);
        if (max != null) builder.indent(2).append("if (bean.{}.doubleValue() > {}) errors.add({}, {});\n", field.getName(), max.value(), pathLiteral, variable(max.message()));
    }

    private void buildMapValidation(CodeBuilder builder, Field field, String pathLiteral, String parentPath) {
        buildSizeValidation(builder, field, pathLiteral);

        Class<?> valueClass = GenericTypes.mapValueClass(field.getGenericType());
        if (!isValueClass(valueClass)) {
            String method = validateMethod(valueClass, path(field, parentPath));
            builder.indent(2).append("for (java.util.Iterator iterator = bean.{}.entrySet().iterator(); iterator.hasNext(); ) {\n", field.getName())
                   .indent(3).append("java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();\n")
                   .indent(3).append("{} value = ({}) entry.getValue();\n", type(valueClass), type(valueClass))
                   .indent(3).append("if (value != null) {}(value, errors, partial);\n", method)
                   .indent(2).append("}\n");
        }
    }

    private void buildListValidation(CodeBuilder builder, Field field, String pathLiteral, String parentPath) {
        buildSizeValidation(builder, field, pathLiteral);

        Class<?> valueClass = GenericTypes.listValueClass(field.getGenericType());
        if (!isValueClass(valueClass)) {
            String method = validateMethod(valueClass, path(field, parentPath));
            builder.indent(2).append("for (java.util.Iterator iterator = bean.{}.iterator(); iterator.hasNext(); ) {\n", field.getName())
                   .indent(3).append("{} value = ({}) iterator.next();\n", type(valueClass), type(valueClass))
                   .indent(3).append("if (value != null) {}(value, errors, partial);\n", method)
                   .indent(2).append("}\n");
        }
    }

    private void buildStringValidation(CodeBuilder builder, Field field, String pathLiteral) {
        NotBlank notBlank = field.getDeclaredAnnotation(NotBlank.class);
        if (notBlank != null) builder.indent(2).append("if (bean.{}.isBlank()) errors.add({}, {});\n", field.getName(), pathLiteral, variable(notBlank.message()));

        Length length = field.getDeclaredAnnotation(Length.class);
        if (length != null) {
            if (length.min() > -1) builder.indent(2).append("if (bean.{}.length() < {}) errors.add({}, {});\n", field.getName(), length.min(), pathLiteral, variable(length.message()));
            if (length.max() > -1) builder.indent(2).append("if (bean.{}.length() > {}) errors.add({}, {});\n", field.getName(), length.max(), pathLiteral, variable(length.message()));
        }

        Pattern pattern = field.getDeclaredAnnotation(Pattern.class);
        if (pattern != null) {
            String patternFieldName = field.getName() + "Pattern" + (index++);
            this.builder.addField("private final java.util.regex.Pattern {} = java.util.regex.Pattern.compile({});", patternFieldName, variable(pattern.value()));
            builder.indent(2).append("if (!this.{}.matcher(bean.{}).matches()) errors.add({}, {});\n", patternFieldName, field.getName(), pathLiteral, variable(pattern.message()));
        }
    }

    private void buildSizeValidation(CodeBuilder builder, Field field, String pathLiteral) {
        Size size = field.getDeclaredAnnotation(Size.class);
        if (size != null) {
            if (size.min() > -1) builder.indent(2).append("if (bean.{}.size() < {}) errors.add({}, {});\n", field.getName(), size.min(), pathLiteral, variable(size.message()));
            if (size.max() > -1) builder.indent(2).append("if (bean.{}.size() > {}) errors.add({}, {});\n", field.getName(), size.max(), pathLiteral, variable(size.message()));
        }
    }

    private String path(Field field, String parentPath) {
        String path = fieldNameProvider.apply(field);
        if (parentPath == null) return path;
        return parentPath + "." + path;
    }

    private void validate(Class<?> beanClass) {
        try {
            Object beanWithDefaultValues = beanClass.getDeclaredConstructor().newInstance();
            for (Field field : Classes.instanceFields(beanClass)) {
                validateAnnotations(field, beanWithDefaultValues);
                Class<?> targetClass = targetValidationClass(field);
                if (!isValueClass(targetClass))
                    validate(targetClass);
            }
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private boolean hasValidationAnnotation(Field field) {
        boolean hasAnnotation = field.isAnnotationPresent(NotNull.class)
                || field.isAnnotationPresent(NotBlank.class)
                || field.isAnnotationPresent(Length.class)
                || field.isAnnotationPresent(Max.class)
                || field.isAnnotationPresent(Min.class)
                || field.isAnnotationPresent(Pattern.class)
                || field.isAnnotationPresent(Size.class);
        if (hasAnnotation) return true;

        Class<?> targetClass = targetValidationClass(field);
        if (!isValueClass(targetClass)) {
            for (Field valueField : Classes.instanceFields(targetClass)) {
                if (hasValidationAnnotation(valueField)) return true;
            }
        }
        return false;
    }

    private Class<?> targetValidationClass(Field field) {
        Type fieldType = field.getGenericType();
        Class<?> targetClass;
        if (GenericTypes.isList(fieldType)) {
            targetClass = GenericTypes.listValueClass(fieldType);
        } else if (GenericTypes.isMap(fieldType)) {
            targetClass = GenericTypes.mapValueClass(fieldType);
        } else {
            targetClass = GenericTypes.rawClass(fieldType);
        }
        return targetClass;
    }

    private void validateAnnotations(Field field, Object beanWithDefaultValues) throws IllegalAccessException {
        Type fieldType = field.getGenericType();

        if (!field.isAnnotationPresent(NotNull.class) && field.get(beanWithDefaultValues) != null)
            throw new Error(format("field with default value must have @NotNull, field={}, fieldType={}", Fields.path(field), fieldType.getTypeName()));

        Size size = field.getDeclaredAnnotation(Size.class);
        if (size != null && !GenericTypes.isList(fieldType) && !GenericTypes.isMap(fieldType))
            throw new Error(format("@Size must on List<?> or Map<String, ?>, field={}, fieldType={}", Fields.path(field), fieldType.getTypeName()));

        NotBlank notBlank = field.getDeclaredAnnotation(NotBlank.class);
        if (notBlank != null && !String.class.equals(fieldType))
            throw new Error(format("@NotBlank must on String, field={}, fieldType={}", Fields.path(field), fieldType.getTypeName()));

        Length length = field.getDeclaredAnnotation(Length.class);
        if (length != null && !String.class.equals(fieldType))
            throw new Error(format("@Length must on String, field={}, fieldType={}", Fields.path(field), fieldType.getTypeName()));

        Pattern pattern = field.getDeclaredAnnotation(Pattern.class);
        if (pattern != null) {
            if (!String.class.equals(fieldType)) throw new Error(format("@Pattern must on String, field={}, fieldType={}", Fields.path(field), fieldType.getTypeName()));
            try {
                java.util.regex.Pattern.compile(pattern.value());
            } catch (PatternSyntaxException e) {
                throw new Error(format("@Pattern has invalid pattern, pattern={}, field={}, fieldType={}", pattern.value(), Fields.path(field), fieldType.getTypeName()), e);
            }
        }

        Class<?> fieldClass = GenericTypes.rawClass(fieldType);
        Max max = field.getDeclaredAnnotation(Max.class);
        if (max != null && !Number.class.isAssignableFrom(fieldClass))
            throw new Error(format("@Max must on Number, field={}, fieldType={}", Fields.path(field), fieldType.getTypeName()));

        Min min = field.getDeclaredAnnotation(Min.class);
        if (min != null && !Number.class.isAssignableFrom(fieldClass))
            throw new Error(format("@Min must on Number, field={}, fieldType={}", Fields.path(field), fieldType.getTypeName()));
    }

    private boolean isValueClass(Class<?> fieldClass) {
        return String.class.equals(fieldClass)
                || Number.class.isAssignableFrom(fieldClass)
                || Boolean.class.equals(fieldClass)
                || LocalDate.class.equals(fieldClass)
                || LocalDateTime.class.equals(fieldClass)
                || ZonedDateTime.class.equals(fieldClass)
                || Instant.class.equals(fieldClass)
                || fieldClass.isEnum()
                || "org.bson.types.ObjectId".equals(fieldClass.getCanonicalName()); // not depends on mongo jar if application doesn't include mongo driver
    }
}
