package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.impl.validate.Validator;
import core.framework.impl.validate.ValidatorBuilder;

/**
 * @author neo
 */
final class RepositoryEntityValidator<T> {
    private final Validator validator;

    RepositoryEntityValidator(Class<T> entityClass) {
        validator = new ValidatorBuilder(entityClass, field -> field.getDeclaredAnnotation(Column.class).name()).build();
    }

    void validate(T entity) {
        validator.validate(entity);
    }

    void partialValidate(T entity) {
        validator.partialValidate(entity);
    }
}
