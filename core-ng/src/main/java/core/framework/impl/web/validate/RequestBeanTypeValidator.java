package core.framework.impl.web.validate;

import core.framework.impl.validate.type.JAXBTypeValidator;

import java.lang.reflect.Type;

/**
 * @author neo
 */
final class RequestBeanTypeValidator extends JAXBTypeValidator {
    RequestBeanTypeValidator(Type instanceType) {
        super(instanceType);
        validator.allowTopLevelList = true;
    }
}
