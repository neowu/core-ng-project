package core.framework.impl.template;

import core.framework.impl.validate.type.DataTypeValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author neo
 */
public class ModelClassValidator {
    private final DataTypeValidator validator;

    public ModelClassValidator(Class<?> modelClass) {
        validator = new DataTypeValidator(modelClass);
        validator.allowedValueClass = this::allowedValueClass;
        validator.allowChildListAndMap = true;
        validator.allowChildObject = true;
        validator.allowTopLevelList = false;
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
