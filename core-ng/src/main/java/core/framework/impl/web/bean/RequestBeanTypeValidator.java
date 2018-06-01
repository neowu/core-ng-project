package core.framework.impl.web.bean;

import core.framework.impl.validate.type.JSONTypeValidator;

import java.lang.reflect.Type;

/**
 * @author neo
 */
final class RequestBeanTypeValidator extends JSONTypeValidator {
    private final BeanClassNameValidator classNameValidator;

    RequestBeanTypeValidator(Type instanceType, BeanClassNameValidator classNameValidator) {
        super(instanceType);
        validator.allowTopLevelList = true;
        this.classNameValidator = classNameValidator;
    }

    @Override
    public void visitEnum(Class<?> enumClass, String parentPath) {
        super.visitEnum(enumClass, parentPath);
        classNameValidator.validateBeanClass(enumClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        classNameValidator.validateBeanClass(objectClass);
    }
}
