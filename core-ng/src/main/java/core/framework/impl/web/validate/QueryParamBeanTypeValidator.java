package core.framework.impl.web.validate;

import core.framework.impl.validate.type.JAXBTypeValidator;

import java.lang.reflect.Type;

/**
 * @author neo
 */
final class QueryParamBeanTypeValidator extends JAXBTypeValidator {
    QueryParamBeanTypeValidator(Type instanceType) {
        super(instanceType);
        validator.allowChild = false;
    }
}
