package core.framework.impl.validate;

import core.framework.api.util.Exceptions;
import core.framework.api.validate.Length;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;
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
            if (isValueClass(targetClass)) {
                return new Validator(null); // not validate value List
            }
        } else {
            targetClass = GenericTypes.rawClass(instanceType);
        }

        Optional<ObjectValidator> objectValidator = createObjectValidator(targetClass, null);

        if (!objectValidator.isPresent()) {
            return new Validator(null);
        }

        if (GenericTypes.isList(instanceType)) {
            return new Validator(new ListValidator(objectValidator.get()));
        }

        return new Validator(objectValidator.get());
    }

    private Optional<ObjectValidator> createObjectValidator(Class<?> instanceClass, String parentPath) {
        ObjectValidator validator = new ObjectValidator();
        for (Field field : instanceClass.getFields()) {
            createValidators(field, validator, parentPath);
        }
        if (validator.empty()) return Optional.empty();
        return Optional.of(validator);
    }

    private String fieldPath(String parentPath, Field field) {
        String fieldName = fieldNameProvider.apply(field);
        if (parentPath == null) return fieldName;
        return parentPath + "." + fieldName;
    }

    private void createValidators(Field field, ObjectValidator validator, String parentPath) {
        Class<?> fieldClass = field.getType();
        NotNull notNull = field.getDeclaredAnnotation(NotNull.class);
        if (notNull != null) {
            validator.add(field, new NotNullValidator(fieldPath(parentPath, field), notNull));
        }

        Length length = field.getDeclaredAnnotation(Length.class);
        if (length != null) {
            if (!String.class.equals(fieldClass) && !List.class.equals(fieldClass) && !Map.class.equals(fieldClass)) {
                throw Exceptions.error("@Length must on String, List<> or Map<>, field={}", field);
            }
            validator.add(field, new LengthValidator(fieldPath(parentPath, field), length));
        }

        Min min = field.getDeclaredAnnotation(Min.class);
        if (min != null) {
            if (!Number.class.isAssignableFrom(fieldClass)) {
                throw Exceptions.error("@Min must on numeric field, field={}", field);
            }
            validator.add(field, new MinValidator(fieldPath(parentPath, field), min));
        }

        Max max = field.getDeclaredAnnotation(Max.class);
        if (max != null) {
            if (!Number.class.isAssignableFrom(fieldClass)) {
                throw Exceptions.error("@Max must on numeric field, field={}", field);
            }
            validator.add(field, new MaxValidator(fieldPath(parentPath, field), max));
        }

        if (!isValueClass(fieldClass)) {
            createFieldValidator(field, validator, parentPath);
        }
    }

    private void createFieldValidator(Field field, ObjectValidator objectValidator, String parentPath) {
        Type fieldType = field.getGenericType();

        if (GenericTypes.isList(fieldType)) {
            Class<?> targetClass = GenericTypes.listValueClass(fieldType);
            if (isValueClass(targetClass)) return;

            createObjectValidator(targetClass, fieldPath(parentPath, field))
                .ifPresent(validator -> objectValidator.add(field, new ListValidator(validator)));
        } else if (GenericTypes.isMap(fieldType)) {
            Class<?> targetClass = GenericTypes.mapValueClass(fieldType);
            if (isValueClass(targetClass)) return;

            createObjectValidator(targetClass, fieldPath(parentPath, field))
                .ifPresent(validator -> objectValidator.add(field, new MapValidator(validator)));
        } else {
            Class<?> targetClass = GenericTypes.rawClass(fieldType);

            createObjectValidator(targetClass, fieldPath(parentPath, field))
                .ifPresent(validator -> objectValidator.add(field, validator));
        }
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
