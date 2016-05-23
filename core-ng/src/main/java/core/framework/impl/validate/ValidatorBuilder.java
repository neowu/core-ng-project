package core.framework.impl.validate;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.validate.Length;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotEmpty;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import core.framework.api.validate.Size;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.Fields;
import core.framework.impl.reflect.GenericTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author neo
 */
public class ValidatorBuilder {
    private final Type instanceType;
    private final Function<Field, String> fieldNameProvider;

    public ValidatorBuilder(Type instanceType, Function<Field, String> fieldNameProvider) {
        this.instanceType = instanceType;
        this.fieldNameProvider = fieldNameProvider;
    }

    public Validator build() {
        Class<?> targetClass;
        if (GenericTypes.isList(instanceType)) { // type validator ensured list can only be generic type in advance
            targetClass = GenericTypes.listValueClass(instanceType);
        } else if (GenericTypes.isOptional(instanceType)) {
            targetClass = GenericTypes.optionalValueClass(instanceType);
        } else {
            targetClass = GenericTypes.rawClass(instanceType);
        }

        if (isValueClass(targetClass)) return new Validator(null); // not validate top level value, since no place to put annotation

        Optional<ObjectValidator> objectValidator = createObjectValidator(targetClass, null);
        if (objectValidator.isPresent()) {
            if (GenericTypes.isList(instanceType)) {
                return new Validator(new ListValidator(Lists.newArrayList(objectValidator.get())));
            } else if (GenericTypes.isOptional(instanceType)) {
                return new Validator(new OptionalValidator(Lists.newArrayList(objectValidator.get())));
            } else {
                return new Validator(objectValidator.get());
            }
        }

        return new Validator(null);
    }

    private Optional<ObjectValidator> createObjectValidator(Class<?> instanceClass, String parentPath) {
        Map<Field, List<FieldValidator>> validators = Maps.newLinkedHashMap();
        for (Field field : Classes.instanceFields(instanceClass)) {
            createValidators(field, parentPath)
                .ifPresent(fieldValidators -> validators.put(field, fieldValidators));
        }
        if (validators.isEmpty()) return Optional.empty();
        return Optional.of(new ObjectValidator(validators));
    }

    private String fieldPath(String parentPath, Field field) {
        String fieldName = fieldNameProvider.apply(field);
        if (parentPath == null) return fieldName;
        return parentPath + "." + fieldName;
    }

    private Optional<List<FieldValidator>> createValidators(Field field, String parentPath) {
        List<FieldValidator> validators = Lists.newArrayList();

        Type fieldType = field.getGenericType();

        createNotNullValidator(field, parentPath).ifPresent(validators::add);
        createSizeValidator(field, parentPath, fieldType).ifPresent(validators::add);

        if (GenericTypes.isList(fieldType)) {
            List<FieldValidator> valueValidators = Lists.newArrayList();
            addValidators(field, parentPath, GenericTypes.listValueClass(fieldType), valueValidators);
            if (!valueValidators.isEmpty()) validators.add(new ListValidator(valueValidators));
        } else if (GenericTypes.isMap(fieldType)) {
            List<FieldValidator> valueValidators = Lists.newArrayList();
            addValidators(field, parentPath, GenericTypes.mapValueClass(fieldType), valueValidators);
            if (!valueValidators.isEmpty()) validators.add(new MapValidator(valueValidators));
        } else if (GenericTypes.isOptional(fieldType)) {
            List<FieldValidator> valueValidators = Lists.newArrayList();
            addValidators(field, parentPath, GenericTypes.optionalValueClass(fieldType), valueValidators);
            if (!valueValidators.isEmpty()) validators.add(new OptionalValidator(valueValidators));
        } else {
            Class<?> fieldClass = GenericTypes.rawClass(fieldType);
            addValidators(field, parentPath, fieldClass, validators);
        }

        if (validators.isEmpty()) return Optional.empty();
        return Optional.of(validators);
    }

    private void addValidators(Field field, String parentPath, Class<?> targetClass, List<FieldValidator> validators) {
        createNotEmptyValidator(field, parentPath, targetClass).ifPresent(validators::add);
        createPatternValidator(field, parentPath, targetClass).ifPresent(validators::add);
        createLengthValidator(field, parentPath, targetClass).ifPresent(validators::add);
        createMinValidator(field, parentPath, targetClass).ifPresent(validators::add);
        createMaxValidator(field, parentPath, targetClass).ifPresent(validators::add);

        if (!isValueClass(targetClass))
            createObjectValidator(targetClass, fieldPath(parentPath, field)).ifPresent(validators::add);
    }

    private Optional<NotNullValidator> createNotNullValidator(Field field, String parentPath) {
        NotNull notNull = field.getDeclaredAnnotation(NotNull.class);
        if (notNull != null) return Optional.of(new NotNullValidator(fieldPath(parentPath, field), notNull.message()));
        return Optional.empty();
    }

    private Optional<NotEmptyValidator> createNotEmptyValidator(Field field, String parentPath, Class<?> targetClass) {
        NotEmpty notEmpty = field.getDeclaredAnnotation(NotEmpty.class);
        if (notEmpty != null) {
            if (!String.class.equals(targetClass))
                throw Exceptions.error("@NotEmpty must on String, Optional<String>, List<String> or Map<String, String>, field={}, fieldClass={}", Fields.path(field), targetClass.getCanonicalName());
            return Optional.of(new NotEmptyValidator(fieldPath(parentPath, field), notEmpty.message()));
        }
        return Optional.empty();
    }

    private Optional<PatternValidator> createPatternValidator(Field field, String parentPath, Class<?> targetClass) {
        Pattern pattern = field.getDeclaredAnnotation(Pattern.class);
        if (pattern != null) {
            if (!String.class.equals(targetClass))
                throw Exceptions.error("@Pattern must on String, Optional<String>, List<String> or Map<String, String>, field={}, fieldClass={}", Fields.path(field), targetClass.getCanonicalName());
            return Optional.of(new PatternValidator(pattern.value(), fieldPath(parentPath, field), pattern.message()));
        }
        return Optional.empty();
    }

    private Optional<LengthValidator> createLengthValidator(Field field, String parentPath, Class<?> targetClass) {
        Length length = field.getDeclaredAnnotation(Length.class);
        if (length != null) {
            if (!String.class.equals(targetClass))
                throw Exceptions.error("@Length must on String, Optional<String>, List<String> or Map<String, String>, field={}, fieldClass={}", Fields.path(field), targetClass.getCanonicalName());
            return Optional.of(new LengthValidator(fieldPath(parentPath, field), length));
        }
        return Optional.empty();
    }

    private Optional<SizeValidator> createSizeValidator(Field field, String parentPath, Type fieldType) {
        Size size = field.getDeclaredAnnotation(Size.class);
        if (size != null) {
            if (!GenericTypes.isList(fieldType) && !GenericTypes.isMap(fieldType))
                throw Exceptions.error("@Size must on List<?> or Map<String, ?>, field={}, fieldType={}", Fields.path(field), fieldType.getTypeName());
            return Optional.of(new SizeValidator(fieldPath(parentPath, field), size));
        }
        return Optional.empty();
    }

    private Optional<MaxValidator> createMaxValidator(Field field, String parentPath, Class<?> targetClass) {
        Max max = field.getDeclaredAnnotation(Max.class);
        if (max != null) {
            if (!Number.class.isAssignableFrom(targetClass))
                throw Exceptions.error("@Max must on numeric field, field={}, fieldClass={}", field, targetClass.getCanonicalName());
            return Optional.of(new MaxValidator(fieldPath(parentPath, field), max));
        }
        return Optional.empty();
    }

    private Optional<MinValidator> createMinValidator(Field field, String parentPath, Class<?> targetClass) {
        Min min = field.getDeclaredAnnotation(Min.class);
        if (min != null) {
            if (!Number.class.isAssignableFrom(targetClass))
                throw Exceptions.error("@Min must on numeric field, field={}, fieldClass={}", field, targetClass.getCanonicalName());
            return Optional.of(new MinValidator(fieldPath(parentPath, field), min));
        }
        return Optional.empty();
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
            || Instant.class.equals(fieldClass)
            || Enum.class.isAssignableFrom(fieldClass)
            || "org.bson.types.ObjectId".equals(fieldClass.getCanonicalName()); // not depends on mongo jar if application doesn't include mongo driver
    }
}
