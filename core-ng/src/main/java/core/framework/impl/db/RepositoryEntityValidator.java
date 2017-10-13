package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.impl.validate.Validator;

/**
 * @author neo
 */
final class RepositoryEntityValidator<T> {
    private final Validator validator;

    RepositoryEntityValidator(Class<T> entityClass) {
        validator = new Validator(entityClass, field -> field.getDeclaredAnnotation(Column.class).name());
    }

    void validate(T entity) {
        validator.validate(entity);
    }

    void partialValidate(T entity) {
        validator.partialValidate(entity);
    }
}
