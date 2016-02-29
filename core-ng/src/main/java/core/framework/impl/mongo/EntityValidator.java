package core.framework.impl.mongo;

import core.framework.api.mongo.Field;
import core.framework.api.mongo.Id;
import core.framework.impl.validate.Validator;
import core.framework.impl.validate.ValidatorBuilder;

/**
 * @author neo
 */
public final class EntityValidator<T> {
    private final Validator validator;

    public EntityValidator(Class<T> entityClass) {
        validator = new ValidatorBuilder(entityClass, field -> {
            if (field.isAnnotationPresent(Id.class)) return "_id";
            return field.getDeclaredAnnotation(Field.class).name();
        }).build();
    }

    public void validate(T entity) {
        validator.validate(entity);
    }
}
