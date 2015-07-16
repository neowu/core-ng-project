package core.framework.impl.mongo;

import core.framework.api.mongo.Field;
import core.framework.api.mongo.Id;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.impl.validate.Validator;
import core.framework.impl.validate.ValidatorBuilder;

import java.util.Map;

/**
 * @author neo
 */
public class MongoEntityValidator {
    private final Map<Class<?>, Validator> validators = Maps.newHashMap();

    public void register(Class<?> entityClass) {
        new MongoClassValidator(entityClass).validateEntityClass();

        validators.computeIfAbsent(entityClass,
            key -> new ValidatorBuilder(key, field -> {
                if (field.isAnnotationPresent(Id.class)) return "_id";
                return field.getDeclaredAnnotation(Field.class).name();
            }).build());
    }

    public <T> void validate(T entity) {
        if (entity == null) throw new Error("entity must not be null");

        Validator validator = validators.get(entity.getClass());
        if (validator == null)
            throw Exceptions.error("entity class is not registered, entityClass={}", entity.getClass().getCanonicalName());

        validator.validate(entity);
    }
}
