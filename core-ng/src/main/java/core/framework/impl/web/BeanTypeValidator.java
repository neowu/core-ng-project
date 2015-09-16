package core.framework.impl.web;

import core.framework.impl.validate.type.JAXBTypeValidator;

import java.lang.reflect.Type;

/**
 * @author neo
 */
final class BeanTypeValidator extends JAXBTypeValidator {
    BeanTypeValidator(Type instanceType) {
        super(instanceType);
        validator.allowTopLevelList = true;
    }
}
