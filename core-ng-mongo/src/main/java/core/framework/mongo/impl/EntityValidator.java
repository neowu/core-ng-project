package core.framework.mongo.impl;

import core.framework.impl.validate.Validator;
import core.framework.mongo.Field;
import core.framework.mongo.Id;

/**
 * @author neo
 */
final class EntityValidator<T> {
    private final Validator validator;

    EntityValidator(Class<T> entityClass) {
        validator = new Validator(entityClass, field -> {
            if (field.isAnnotationPresent(Id.class)) return "_id";
            return field.getDeclaredAnnotation(Field.class).name();
        });
    }

    void validate(T entity) {
        validator.validate(entity);
    }
}
