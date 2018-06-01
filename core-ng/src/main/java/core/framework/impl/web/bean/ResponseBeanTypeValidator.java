package core.framework.impl.web.bean;

import core.framework.impl.validate.type.JSONTypeValidator;
import core.framework.util.Sets;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author neo
 */
public final class ResponseBeanTypeValidator {
    private final Set<Type> validatedTypes = Sets.newConcurrentHashSet();
    private final BeanClassNameValidator classNameValidator;

    public ResponseBeanTypeValidator(BeanClassNameValidator classNameValidator) {
        this.classNameValidator = classNameValidator;
    }

    public void validate(Type beanType) {
        if (!validatedTypes.contains(beanType)) {
            new TypeValidator(beanType, classNameValidator).validate();
            validatedTypes.add(beanType);
        }
    }

    static class TypeValidator extends JSONTypeValidator {
        private final BeanClassNameValidator classNameValidator;

        TypeValidator(Type beanType, BeanClassNameValidator classNameValidator) {
            super(beanType);
            validator.allowTopLevelOptional = true;
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
}
