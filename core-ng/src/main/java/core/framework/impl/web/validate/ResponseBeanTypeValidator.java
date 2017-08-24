package core.framework.impl.web.validate;

import core.framework.impl.validate.type.JAXBTypeValidator;

import java.lang.reflect.Type;

/**
 * @author neo
 */
final class ResponseBeanTypeValidator extends JAXBTypeValidator {
    ResponseBeanTypeValidator(Type beanType) {
        super(beanType);
        validator.allowTopLevelOptional = true;
        validator.allowTopLevelList = true;
    }
}
