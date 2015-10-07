package core.framework.impl.search;

import core.framework.impl.validate.Validator;
import core.framework.impl.validate.ValidatorBuilder;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
final class DocumentValidator<T> {
    private final Validator validator;

    DocumentValidator(Class<T> documentClass) {
        validator = new ValidatorBuilder(documentClass, field -> field.getDeclaredAnnotation(XmlElement.class).name()).build();
    }

    public void validate(T document) {
        validator.validate(document);
    }
}
