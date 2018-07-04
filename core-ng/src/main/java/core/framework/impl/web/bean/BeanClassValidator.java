package core.framework.impl.web.bean;

import core.framework.impl.validate.type.JSONTypeValidator;

/**
 * @author neo
 */
final class BeanClassValidator extends JSONTypeValidator {
    private final BeanClassNameValidator classNameValidator;

    BeanClassValidator(Class<?> beanClass, BeanClassNameValidator classNameValidator) {
        super(beanClass);
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
