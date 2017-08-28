package core.framework.impl.cache;

import core.framework.impl.validate.type.DataTypeValidator;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
class CacheTypeValidator {
    private final DataTypeValidator validator;

    CacheTypeValidator(Type instanceType) {
        validator = new DataTypeValidator(instanceType);
        validator.allowedValueClass = this::allowedValueClass;
        validator.allowChild = true;
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
                || ZonedDateTime.class.equals(valueClass)
                || valueClass.isEnum()
                || "org.bson.types.ObjectId".equals(valueClass.getCanonicalName()); // not depends on mongo jar if application doesn't include mongo driver;
    }
}
