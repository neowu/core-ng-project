package core.framework.impl.cache;

import core.framework.impl.validate.type.TypeValidator;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author neo
 */
class CacheTypeValidator {
    private final TypeValidator validator;

    public CacheTypeValidator(Type instanceType) {
        validator = new TypeValidator(instanceType);
        validator.allowedValueClass = this::allowedValueClass;
        validator.allowChildListAndMap = true;
        validator.allowChildObject = true;
        validator.allowTopLevelList = true;
        validator.allowTopLevelValue = true;
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
            || Instant.class.equals(valueClass)
            || Enum.class.isAssignableFrom(valueClass)
            || "org.bson.types.ObjectId".equals(valueClass.getCanonicalName()); // not depends on mongo jar if application doesn't include mongo driver;
    }
}
