package core.framework.mongo.impl;

import core.framework.internal.validate.Validator;

/**
 * @author neo
 */
final class EntityValidator<T> {
    private final Validator validator;

    EntityValidator(Class<T> entityClass) {
        validator = new Validator(entityClass);
    }

    void validate(T entity) {
        validator.validate(entity, false);
    }
}
