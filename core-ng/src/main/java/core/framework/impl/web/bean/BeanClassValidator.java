package core.framework.impl.web.bean;

import core.framework.internal.validate.type.JSONClassValidator;

/**
 * @author neo
 */
final class BeanClassValidator extends JSONClassValidator {
    private final BeanMapperRegistry registry;

    BeanClassValidator(Class<?> beanClass, BeanMapperRegistry registry) {
        super(beanClass);
        this.registry = registry;
    }

    @Override
    public void visitEnum(Class<?> enumClass) {
        super.visitEnum(enumClass);
        registry.validateBeanClassName(enumClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        registry.validateBeanClassName(objectClass);
    }
}
