package core.framework.internal.cache;

import core.framework.internal.validate.BeanClassValidator;

/**
 * @author neo
 */
class CacheClassValidator {
    private final BeanClassValidator validator;

    CacheClassValidator(Class<?> cacheClass) {
        validator = new BeanClassValidator(cacheClass);
        validator.allowChild = true;
    }

    public void validate() {
        validator.validate();
    }
}
