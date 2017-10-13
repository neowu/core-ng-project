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

    public void validate(Type beanType) {
        if (!validatedTypes.contains(beanType)) {
            new TypeValidator(beanType).validate();
            validatedTypes.add(beanType);
        }
    }

    static class TypeValidator extends JSONTypeValidator {
        TypeValidator(Type beanType) {
            super(beanType);
            validator.allowTopLevelOptional = true;
            validator.allowTopLevelList = true;
        }
    }
}
