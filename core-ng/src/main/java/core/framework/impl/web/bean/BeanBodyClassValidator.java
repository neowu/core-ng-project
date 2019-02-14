package core.framework.impl.web.bean;

import core.framework.internal.json.JSONClassValidator;

/**
 * @author neo
 */
final class BeanBodyClassValidator extends JSONClassValidator {
    private final BeanBodyMapperRegistry registry;

    BeanBodyClassValidator(Class<?> beanClass, BeanBodyMapperRegistry registry) {
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
