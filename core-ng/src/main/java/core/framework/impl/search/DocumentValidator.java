package core.framework.impl.search;

import core.framework.api.json.Property;
import core.framework.impl.validate.Validator;

/**
 * @author neo
 */
final class DocumentValidator<T> {
    private final Validator validator;

    DocumentValidator(Class<T> documentClass) {
        validator = new Validator(documentClass, field -> field.getDeclaredAnnotation(Property.class).name());
    }

    public void validate(T document) {
        validator.validate(document);
    }
}
