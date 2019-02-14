package core.framework.impl.web.bean;

import core.framework.internal.json.JSONClassValidator;

/**
 * @author neo
 */
final class BeanClassValidator extends JSONClassValidator {
    private final BeanClassNameValidator beanClassNameValidator;

    BeanClassValidator(Class<?> beanClass, BeanClassNameValidator beanClassNameValidator) {
        super(beanClass);
        this.beanClassNameValidator = beanClassNameValidator;
    }

    @Override
    public void visitEnum(Class<?> enumClass) {
        super.visitEnum(enumClass);
        beanClassNameValidator.validate(enumClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        beanClassNameValidator.validate(objectClass);
    }
}
