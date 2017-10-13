package core.framework.impl.validate;

import core.framework.api.validate.Length;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotEmpty;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import core.framework.api.validate.Size;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.Fields;
import core.framework.impl.reflect.GenericTypes;
import core.framework.util.Exceptions;
import core.framework.util.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;

import static core.framework.impl.asm.Literal.type;
import static core.framework.impl.asm.Literal.variable;

/**
 * @author neo
 */
public class ObjectValidatorBuilder {
    private final Type instanceType;
    private final Function<Field, String> fieldNameProvider;
    DynamicInstanceBuilder<ObjectValidator> builder;
    private int index = 0;

    public ObjectValidatorBuilder(Type instanceType, Function<Field, String> fieldNameProvider) {
        this.instanceType = instanceType;
        this.fieldNameProvider = fieldNameProvider;
    }

    public Optional<ObjectValidator> build() {
        Class<?> targetClass = unwrapInstanceType();
        if (Classes.instanceFields(targetClass).stream().noneMatch(this::hasValidationAnnotation)) return Optional.empty();

        builder = new DynamicInstanceBuilder<>(ObjectValidator.class, targetClass.getTypeName() + "$ObjectValidator");
        String method = validateObjectMethod(targetClass, null);

        CodeBuilder builder = new CodeBuilder().append("public void validate(Object instance, {} errors, boolean partial) {\n", type(ValidationErrors.class));
        if (GenericTypes.isList(instanceType)) {
            builder.indent(1).append("java.util.List list = (java.util.List) instance;\n")
                   .indent(1).append("for (java.util.Iterator iterator = list.iterator(); iterator.hasNext(); ) {\n")
                   .indent(2).append("{} value = ({}) iterator.next();\n", type(targetClass), type(targetClass))
                   .indent(2).append("if (value != null) {}(value, errors, partial);\n", method)
                   .indent(1).append("}\n");
        } else {
            builder.indent(1).append("{}(({}) instance, errors, partial);\n", method, type(instanceType));
        }
        builder.append('}');
        this.builder.addMethod(builder.build());
        return Optional.of(this.builder.build());
    }

    private Class<?> unwrapInstanceType() {
        if (GenericTypes.isList(instanceType)) return GenericTypes.listValueClass(instanceType);
        return GenericTypes.rawClass(instanceType);
    }

    private String validateObjectMethod(Class<?> beanClass, String parentPath) {
        String methodName = "validate" + beanClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder().append("private void {}({} bean, {} errors, boolean partial) {\n", methodName, type(beanClass), type(ValidationErrors.class));
        for (Field field : Classes.instanceFields(beanClass)) {
            if (!hasValidationAnnotation(field)) continue;
            validateAnnotations(field);

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
                String method = validateObjectMethod(fieldClass, path(field, parentPath));
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
            String method = validateObjectMethod(valueClass, path(field, parentPath));
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
            String method = validateObjectMethod(valueClass, path(field, parentPath));
            builder.indent(2).append("for (java.util.Iterator iterator = bean.{}.iterator(); iterator.hasNext(); ) {\n", field.getName())
                   .indent(3).append("{} value = ({}) iterator.next();\n", type(valueClass), type(valueClass))
                   .indent(3).append("if (value != null) {}(value, errors, partial);\n", method)
                   .indent(2).append("}\n");
        }
    }

    private void buildStringValidation(CodeBuilder builder, Field field, String pathLiteral) {
        NotEmpty notEmpty = field.getDeclaredAnnotation(NotEmpty.class);
        if (notEmpty != null) builder.indent(2).append("if ({}.isEmpty(bean.{})) errors.add({}, {});\n", type(Strings.class), field.getName(), pathLiteral, variable(notEmpty.message()));

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

    private void validateAnnotations(Field field) {
        Type fieldType = field.getGenericType();
        Class<?> fieldClass = GenericTypes.rawClass(fieldType);
        String fieldPath = Fields.path(field);

        Size size = field.getDeclaredAnnotation(Size.class);
        if (size != null && !GenericTypes.isList(fieldType) && !GenericTypes.isMap(fieldType))
            throw Exceptions.error("@Size must on List<?> or Map<String, ?>, field={}, fieldClass={}", fieldPath, fieldClass.getCanonicalName());

        NotEmpty notEmpty = field.getDeclaredAnnotation(NotEmpty.class);
        if (notEmpty != null && !String.class.equals(fieldType))
            throw Exceptions.error("@NotEmpty must on String, field={}, fieldClass={}", fieldPath, fieldClass.getCanonicalName());

        Length length = field.getDeclaredAnnotation(Length.class);
        if (length != null && !String.class.equals(fieldType))
            throw Exceptions.error("@Length must on String, field={}, fieldClass={}", fieldPath, fieldClass.getCanonicalName());

        Pattern pattern = field.getDeclaredAnnotation(Pattern.class);
        if (pattern != null) {
            if (!String.class.equals(fieldType)) throw Exceptions.error("@Pattern must on String, field={}, fieldClass={}", fieldPath, fieldClass.getCanonicalName());
            try {
                java.util.regex.Pattern.compile(pattern.value());
            } catch (PatternSyntaxException e) {
                throw Exceptions.error("@Pattern has invalid pattern, pattern={}, field={}, fieldClass={}", pattern.value(), fieldPath, fieldClass.getCanonicalName(), e);
            }
        }

        Max max = field.getDeclaredAnnotation(Max.class);
        if (max != null && !Number.class.isAssignableFrom(fieldClass))
            throw Exceptions.error("@Max must on numeric field, field={}, fieldClass={}", fieldPath, fieldClass.getCanonicalName());

        Min min = field.getDeclaredAnnotation(Min.class);
        if (min != null && !Number.class.isAssignableFrom(fieldClass))
            throw Exceptions.error("@Min must on numeric field, field={}, fieldClass={}", fieldPath, fieldClass.getCanonicalName());
    }

    private boolean hasValidationAnnotation(Field field) {
        boolean hasAnnotation = field.isAnnotationPresent(NotNull.class)
                || field.isAnnotationPresent(NotEmpty.class)
                || field.isAnnotationPresent(Length.class)
                || field.isAnnotationPresent(Max.class)
                || field.isAnnotationPresent(Min.class)
                || field.isAnnotationPresent(Pattern.class)
                || field.isAnnotationPresent(Size.class);
        if (hasAnnotation) return true;

        Type fieldType = field.getGenericType();
        Class<?> targetClass;
        if (GenericTypes.isList(fieldType)) {
            targetClass = GenericTypes.listValueClass(fieldType);
        } else if (GenericTypes.isMap(fieldType)) {
            targetClass = GenericTypes.mapValueClass(fieldType);
        } else {
            targetClass = GenericTypes.rawClass(fieldType);
        }
        if (!isValueClass(targetClass)) {
            for (Field valueField : Classes.instanceFields(targetClass)) {
                if (hasValidationAnnotation(valueField)) return true;
            }
        }
        return false;
    }

    private boolean isValueClass(Class<?> fieldClass) {
        return String.class.equals(fieldClass)
                || Integer.class.equals(fieldClass)
                || Boolean.class.equals(fieldClass)
                || Long.class.equals(fieldClass)
                || Double.class.equals(fieldClass)
                || BigDecimal.class.equals(fieldClass)
                || LocalDate.class.equals(fieldClass)
                || LocalDateTime.class.equals(fieldClass)
                || ZonedDateTime.class.equals(fieldClass)
                || Instant.class.equals(fieldClass)
                || fieldClass.isEnum()
                || "org.bson.types.ObjectId".equals(fieldClass.getCanonicalName()); // not depends on mongo jar if application doesn't include mongo driver
    }
}
