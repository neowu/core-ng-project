package core.framework.impl.web.bean;

import core.framework.impl.validate.type.JSONTypeValidator;

import java.lang.reflect.Type;

/**
 * @author neo
 */
final class RequestBeanTypeValidator extends JSONTypeValidator {
    RequestBeanTypeValidator(Type instanceType) {
        super(instanceType);
        validator.allowTopLevelList = true;
    }
}
