package core.framework.impl.web.bean;

import core.framework.internal.validate.Validator;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class RequestBeanMapper {
    private final BeanMappers beanMappers;
    public final Map<Class<?>, QueryParamMapperHolder<?>> queryParamMappers = Maps.newHashMap();

    public RequestBeanMapper(BeanMappers beanMappers) {
        this.beanMappers = beanMappers;
    }

    public <T> Map<String, String> toParams(Class<T> beanClass, T bean) {
        QueryParamMapperHolder<T> holder = queryParamMapper(beanClass);
        holder.validator.validate(bean, false);
        return holder.mapper.toParams(bean);
    }

    public <T> T fromParams(Class<T> beanClass, Map<String, String> params) {
        QueryParamMapperHolder<T> holder = queryParamMapper(beanClass);
        T bean = holder.mapper.fromParams(params);
        holder.validator.validate(bean, false);
        return bean;
    }

    private <T> QueryParamMapperHolder<T> queryParamMapper(Class<T> beanClass) {
        @SuppressWarnings("unchecked")
        QueryParamMapperHolder<T> holder = (QueryParamMapperHolder<T>) queryParamMappers.get(beanClass);
        if (holder == null) throw new Error("bean class is not registered, please use api().bean() to register, class=" + beanClass.getCanonicalName());
        return holder;
    }

    public <T> void registerQueryParamBean(Class<T> beanClass, BeanClassNameValidator beanClassNameValidator) {
        if (!queryParamMappers.containsKey(beanClass)) {
            new QueryParamClassValidator(beanClass, beanClassNameValidator).validate();
            QueryParamMapper<T> mapper = new QueryParamMapperBuilder<>(beanClass).build();
            var validator = new Validator(beanClass);
            queryParamMappers.put(beanClass, new QueryParamMapperHolder<>(mapper, validator));
        }
    }

    public <T> byte[] toJSON(Class<T> beanClass, T bean) {
        return beanMappers.toJSON(beanClass, bean);
    }

    public <T> T fromJSON(Class<T> beanClass, byte[] body) {
        BeanMapper<T> mapper = beanMappers.mapper(beanClass);
        T bean = mapper.mapper.fromJSON(body);
        mapper.validator.validate(bean, false);
        return bean;
    }

    public void registerRequestBean(Class<?> beanClass, BeanClassNameValidator beanClassNameValidator) {
        beanMappers.register(beanClass, beanClassNameValidator);
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
