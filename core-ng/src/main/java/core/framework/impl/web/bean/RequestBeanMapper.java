package core.framework.impl.web.bean;

import core.framework.internal.validate.Validator;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class RequestBeanMapper {
    private final BeanBodyMapperRegistry registry;
    private final Map<Class<?>, QueryParamMapperHolder<?>> queryParamMappers = Maps.newConcurrentHashMap();

    public RequestBeanMapper(BeanBodyMapperRegistry registry) {
        this.registry = registry;
    }

    public <T> Map<String, String> toParams(Class<T> beanClass, T bean) {
        QueryParamMapperHolder<T> holder = registerQueryParamBean(beanClass);
        holder.validator.validate(bean, false);
        return holder.mapper.toParams(bean);
    }

    public <T> T fromParams(Class<T> beanClass, Map<String, String> params) {
        QueryParamMapperHolder<T> holder = registerQueryParamBean(beanClass);
        T bean = holder.mapper.fromParams(params);
        holder.validator.validate(bean, false);
        return bean;
    }

    @SuppressWarnings("unchecked")
    public <T> QueryParamMapperHolder<T> registerQueryParamBean(Class<T> beanClass) {
        return (QueryParamMapperHolder<T>) queryParamMappers.computeIfAbsent(beanClass, key -> {
            new QueryParamClassValidator(beanClass, registry).validate();
            QueryParamMapper<T> mapper = new QueryParamMapperBuilder<>(beanClass).build();
            var validator = new Validator(beanClass);
            return new QueryParamMapperHolder<>(mapper, validator);
        });
    }

    public <T> byte[] toJSON(Class<T> beanClass, T bean) {
        return registry.toJSON(beanClass, bean);
    }

    public <T> T fromJSON(Class<T> beanClass, byte[] body) {
        BeanBodyMapper<T> mapper = registry.register(beanClass);
        T bean = mapper.mapper.fromJSON(body);
        mapper.validator.validate(bean, false);
        return bean;
    }

    public void registerRequestBean(Class<?> beanClass) {
        registry.register(beanClass);
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
