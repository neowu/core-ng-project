package core.framework.impl.search;

import core.framework.impl.validate.Validator;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
final class DocumentValidator<T> {
    private final Validator validator;

    DocumentValidator(Class<T> documentClass) {
        validator = new Validator(documentClass, field -> field.getDeclaredAnnotation(XmlElement.class).name());
    }

    public void validate(T document) {
        validator.validate(document);
    }
}
