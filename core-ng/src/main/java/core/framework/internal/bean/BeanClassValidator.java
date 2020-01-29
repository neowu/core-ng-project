package core.framework.internal.bean;

import core.framework.internal.json.JSONClassValidator;

/**
 * @author neo
 */
public final class BeanClassValidator extends JSONClassValidator { // bean is used by both web service request/response bean and kafka message class
    private final BeanClassNameValidator beanClassNameValidator;

    public BeanClassValidator(Class<?> beanClass, BeanClassNameValidator beanClassNameValidator) {
        super(beanClass);
        this.beanClassNameValidator = beanClassNameValidator;
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        super.visitClass(objectClass, path);
        beanClassNameValidator.validate(objectClass);
    }

    @Override
    public void visitEnum(Class<?> enumClass) {
        super.visitEnum(enumClass);
        beanClassNameValidator.validate(enumClass);
    }
}
