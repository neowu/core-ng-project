package core.framework.impl.mongo;

import core.framework.api.mongo.Field;
import core.framework.api.mongo.Id;
import core.framework.impl.validate.Validator;

/**
 * @author neo
 */
public final class EntityValidator<T> {
    private final Validator validator;

    public EntityValidator(Class<T> entityClass) {
        validator = new Validator(entityClass, field -> {
            if (field.isAnnotationPresent(Id.class)) return "_id";
            return field.getDeclaredAnnotation(Field.class).name();
        });
    }

    public void validate(T entity) {
        validator.validate(entity);
    }
}
