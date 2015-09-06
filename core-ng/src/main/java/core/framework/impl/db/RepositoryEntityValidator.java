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

    public void validate(T entity) {
        validator.validate(entity);
    }

    public void partialValidate(T entity) {
        validator.partialValidate(entity);
    }
}
