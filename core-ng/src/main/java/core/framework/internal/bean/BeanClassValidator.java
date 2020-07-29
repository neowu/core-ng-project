package core.framework.internal.bean;

import core.framework.internal.json.JSONClassValidator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author neo
 */
public final class BeanClassValidator { // bean is used by both web service request/response bean and kafka message class
    public final BeanClassNameValidator beanClassNameValidator = new BeanClassNameValidator();
    private final Set<Class<?>> validatedBeanClasses = new HashSet<>();

    public void validate(Class<?> beanClass) {
        boolean added = validatedBeanClasses.add(beanClass);
        if (!added) return;

        var validator = new JSONClassValidator(beanClass) {
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
        };
        validator.validate();
    }
}
