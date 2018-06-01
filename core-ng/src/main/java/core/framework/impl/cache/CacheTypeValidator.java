package core.framework.impl.cache;

import core.framework.impl.validate.type.DataTypeValidator;

import java.lang.reflect.Type;

/**
 * @author neo
 */
class CacheTypeValidator {
    private final DataTypeValidator validator;

    CacheTypeValidator(Type instanceType) {
        validator = new DataTypeValidator(instanceType);
        validator.allowChild = true;
        validator.allowTopLevelList = true;
        validator.allowTopLevelValue = true;
    }

    public void validate() {
        validator.validate();
    }
}
