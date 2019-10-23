package core.framework.internal.bean;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.validate.ClassVisitor;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public final class BeanClassValidator implements ClassVisitor { // bean is used by both web service request/response bean and kafka message class
    private final BeanClassNameValidator beanClassNameValidator;
    private final JSONClassValidator validator;

    public BeanClassValidator(Class<?> beanClass, BeanClassNameValidator beanClassNameValidator) {
        validator = new JSONClassValidator(beanClass);
        this.beanClassNameValidator = beanClassNameValidator;
    }

    public void validate() {
        validator.validate();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        validator.visitClass(objectClass, path);
        beanClassNameValidator.validate(objectClass);
    }

    @Override
    public void visitField(Field field, String parentPath) {
        validator.visitField(field, parentPath);
    }

    @Override
    public void visitEnum(Class<?> enumClass) {
        validator.visitEnum(enumClass);
        beanClassNameValidator.validate(enumClass);
    }
}
