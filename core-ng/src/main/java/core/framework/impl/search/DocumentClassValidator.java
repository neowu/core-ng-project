package core.framework.impl.search;

import core.framework.impl.validate.type.JAXBTypeValidator;

/**
 * @author neo
 */
final class DocumentClassValidator extends JAXBTypeValidator {
    DocumentClassValidator(Class<?> documentClass) {
        super(documentClass);
    }
}
