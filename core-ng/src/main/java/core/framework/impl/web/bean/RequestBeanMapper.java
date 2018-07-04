package core.framework.impl.web.bean;

import core.framework.api.json.Property;
import core.framework.api.web.service.QueryParam;
import core.framework.impl.validate.Validator;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class RequestBeanMapper {
    private final Map<Class<?>, BeanMapper<?>> requestBeanMappers = Maps.newConcurrentHashMap();
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
            var validator = new Validator(beanClass, field -> field.getDeclaredAnnotation(QueryParam.class).name());
            return new QueryParamMapperHolder<>(mapper, validator);
        });
    }

    public <T> byte[] toJSON(Class<T> beanClass, T bean) {
        BeanMapper<T> holder = registerRequestBean(beanClass);
        holder.validator.validate(bean);
        return holder.writer.toJSON(bean);
    }

    public <T> T fromJSON(Class<T> beanClass, byte[] body) {
        BeanMapper<T> holder = registerRequestBean(beanClass);
        T bean = holder.reader.fromJSON(body);
        holder.validator.validate(bean);
        return bean;
    }

    @SuppressWarnings("unchecked")
    public <T> BeanMapper<T> registerRequestBean(Class<T> beanClass) {
        return (BeanMapper<T>) requestBeanMappers.computeIfAbsent(beanClass, type -> {
            new BeanClassValidator(beanClass, classNameValidator).validate();
            return new BeanMapper<>(beanClass, new Validator(beanClass, field -> field.getDeclaredAnnotation(Property.class).name()));
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
