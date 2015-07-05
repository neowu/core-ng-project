package core.framework.impl.web;

import core.framework.api.util.Maps;
import core.framework.api.util.Types;
import core.framework.api.web.exception.ValidationException;
import core.framework.impl.validate.ValidationResult;
import core.framework.impl.validate.Validator;
import core.framework.impl.validate.ValidatorBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class BeanValidator {
    private final Map<Type, Validator> validators = Maps.newConcurrentHashMap();

    public void register(Type instanceType) {
        if (!validators.containsKey(instanceType)) {
            new BeanTypeValidator(instanceType).validate();
            Validator validator = new ValidatorBuilder(instanceType, field -> field.getDeclaredAnnotation(XmlElement.class).name()).build();
            validators.putIfAbsent(instanceType, validator);
        }
    }

    public <T> T validate(T bean) throws ValidationException {
        Type beanType;
        if (bean instanceof List) {
            if (((List) bean).isEmpty()) return bean;
            Object item = ((List) bean).get(0);
            if (item == null) throw new ValidationException("list element must not be null", null);
            Class<?> valueClass = item.getClass();
            beanType = Types.list(valueClass);
        } else {
            beanType = bean.getClass();
        }

        Validator validator = validators.get(beanType);
        ValidationResult result = validator.validate(bean);
        if (!result.isValid())
            throw new ValidationException("failed to validate, please see field errors", result.errors);
        return bean;
    }
}
