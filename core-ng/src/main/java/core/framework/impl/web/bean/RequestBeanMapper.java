package core.framework.impl.web.bean;

import core.framework.api.json.Property;
import core.framework.api.web.service.QueryParam;
import core.framework.impl.json.JSONMapper;
import core.framework.impl.validate.Validator;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public final class RequestBeanMapper {
    private final Map<Class<?>, Validator> requestBeanValidators = Maps.newConcurrentHashMap();
    private final Map<Class<?>, QueryParamMapperHolder<?>> queryParamMappers = Maps.newConcurrentHashMap();
    private final BeanClassNameValidator classNameValidator;

    public RequestBeanMapper(BeanClassNameValidator classNameValidator) {
        this.classNameValidator = classNameValidator;
    }

    public <T> Map<String, String> toParams(Class<T> beanClass, T bean) {
        QueryParamMapperHolder<T> holder = registerQueryParamBean(beanClass);
        holder.validator.validate(bean);
        return holder.mapper.toParams(bean);
    }

    public <T> T fromParams(Class<T> beanClass, Map<String, String> params) {
        QueryParamMapperHolder<T> holder = registerQueryParamBean(beanClass);
        T bean = holder.mapper.fromParams(params);
        holder.validator.validate(bean);
        return bean;
    }

    @SuppressWarnings("unchecked")
    public <T> QueryParamMapperHolder<T> registerQueryParamBean(Class<T> beanClass) {
        return (QueryParamMapperHolder<T>) queryParamMappers.computeIfAbsent(beanClass, key -> {
            new QueryParamBeanClassValidator(beanClass, classNameValidator).validate();
            QueryParamMapper<T> mapper = new QueryParamMapperBuilder<>(beanClass).build();
            Validator validator = new Validator(beanClass, field -> field.getDeclaredAnnotation(QueryParam.class).name());
            return new QueryParamMapperHolder<>(mapper, validator);
        });
    }

    public <T> byte[] toJSON(Class<T> beanClass, T bean) {
        registerRequestBean(beanClass).validate(bean);
        return JSONMapper.toJSON(bean);
    }

    public <T> T fromJSON(Class<T> beanClass, byte[] body) {
        Validator validator = registerRequestBean(beanClass);
        T bean = JSONMapper.fromJSON(beanClass, body);
        validator.validate(bean);
        return bean;
    }

    public <T> Validator registerRequestBean(Class<T> beanClass) {
        return requestBeanValidators.computeIfAbsent(beanClass, type -> {
            new RequestBeanClassValidator(beanClass, classNameValidator).validate();
            return new Validator(beanClass, field -> field.getDeclaredAnnotation(Property.class).name());
        });
    }

    static class QueryParamMapperHolder<T> {
        final QueryParamMapper<T> mapper;
        final Validator validator;

        QueryParamMapperHolder(QueryParamMapper<T> mapper, Validator validator) {
            this.mapper = mapper;
            this.validator = validator;
        }
    }
}
