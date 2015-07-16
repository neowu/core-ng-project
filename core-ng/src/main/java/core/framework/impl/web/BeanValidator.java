package core.framework.impl.web;

import core.framework.api.util.Maps;
import core.framework.impl.validate.Validator;
import core.framework.impl.validate.ValidatorBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
public class BeanValidator {
    private final Map<Type, Validator> validators = Maps.newConcurrentHashMap();

    public Validator register(Type instanceType) {
        return validators.computeIfAbsent(instanceType, type -> {
            new BeanTypeValidator(instanceType).validate();
            return new ValidatorBuilder(instanceType, field -> field.getDeclaredAnnotation(XmlElement.class).name()).build();
        });
    }

    public <T> T validate(Type instanceType, T bean) {
        Validator validator = register(instanceType);
        validator.validate(bean);
        return bean;
    }
}
