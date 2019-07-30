package core.framework.impl.web.bean;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.validate.ClassVisitor;

import java.lang.reflect.Field;

/**
 * @author neo
 */
final class BeanClassValidator implements ClassVisitor {
    private final BeanClassNameValidator beanClassNameValidator;
    private final JSONClassValidator validator;

    BeanClassValidator(Class<?> beanClass, BeanClassNameValidator beanClassNameValidator) {
        validator = new JSONClassValidator(beanClass);
        this.beanClassNameValidator = beanClassNameValidator;
    }

    void validate() {
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
