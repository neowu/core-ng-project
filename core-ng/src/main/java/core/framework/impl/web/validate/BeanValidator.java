package core.framework.impl.web.validate;

import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.impl.validate.Validator;
import core.framework.impl.validate.ValidatorBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class BeanValidator {
    private final Map<Type, Validator> requestBeanValidators = Maps.newConcurrentHashMap();
    private final Map<Type, Validator> queryParamBeanValidators = Maps.newConcurrentHashMap();
    private final Set<Type> validatedResponseBeanTypes = Sets.newConcurrentHashSet();

    public Validator registerRequestBeanType(Type beanType) {
        return requestBeanValidators.computeIfAbsent(beanType, type -> {
            new RequestBeanTypeValidator(beanType).validate();
            return new ValidatorBuilder(beanType, field -> field.getDeclaredAnnotation(XmlElement.class).name()).build();
        });
    }

    public <T> T validateRequestBean(Type beanType, T bean) {
        Validator validator = registerRequestBeanType(beanType);
        validator.validate(bean);
        return bean;
    }

    public Validator registerQueryParamBeanType(Type beanType) {
        return queryParamBeanValidators.computeIfAbsent(beanType, type -> {
            new QueryParamBeanTypeValidator(beanType).validate();
            return new ValidatorBuilder(beanType, field -> field.getDeclaredAnnotation(XmlElement.class).name()).build();
        });
    }

    public <T> T validateQueryParamBean(Type beanType, T bean) {
        Validator validator = registerQueryParamBeanType(beanType);
        validator.validate(bean);
        return bean;
    }

    public void validateResponseBeanType(Type beanType) {
        if (!validatedResponseBeanTypes.contains(beanType)) {
            new ResponseBeanTypeValidator(beanType).validate();
            validatedResponseBeanTypes.add(beanType);
        }
    }
}
