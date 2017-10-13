package core.framework.impl.web.bean;

import core.framework.api.json.Property;
import core.framework.api.web.service.QueryParam;
import core.framework.impl.json.JSONMapper;
import core.framework.impl.validate.Validator;
import core.framework.util.Maps;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
public final class RequestBeanMapper {
    private final Map<Type, Validator> requestBeanValidators = Maps.newConcurrentHashMap();
    private final Map<Type, QueryParamMapperHolder<?>> queryParamMappers = Maps.newConcurrentHashMap();

    public <T> Map<String, String> toParams(Type beanType, T bean) {
        QueryParamMapperHolder<T> holder = registerQueryParamBean(beanType);
        holder.validator.validate(bean);
        return holder.mapper.toParams(bean);
    }

    public <T> T fromParams(Type beanType, Map<String, String> params) {
        QueryParamMapperHolder<T> holder = registerQueryParamBean(beanType);
        T bean = holder.mapper.fromParams(params);
        holder.validator.validate(bean);
        return bean;
    }

    @SuppressWarnings("unchecked")
    public <T> QueryParamMapperHolder<T> registerQueryParamBean(Type beanType) {
        return (QueryParamMapperHolder<T>) queryParamMappers.computeIfAbsent(beanType, key -> {
            new QueryParamBeanTypeValidator(beanType).validate();
            QueryParamMapper<T> mapper = new QueryParamMapperBuilder<>((Class<T>) beanType).build();
            Validator validator = new Validator(beanType, field -> field.getDeclaredAnnotation(QueryParam.class).name());
            return new QueryParamMapperHolder<>(mapper, validator);
        });
    }

    public <T> byte[] toJSON(Type beanType, T bean) {
        registerRequestBean(beanType).validate(bean);
        return JSONMapper.toJSON(bean);
    }

    public <T> T fromJSON(Type beanType, byte[] body) {
        Validator validator = registerRequestBean(beanType);
        T bean = JSONMapper.fromJSON(beanType, body);
        validator.validate(bean);
        return bean;
    }

    public Validator registerRequestBean(Type beanType) {
        return requestBeanValidators.computeIfAbsent(beanType, type -> {
            new RequestBeanTypeValidator(beanType).validate();
            return new Validator(beanType, field -> field.getDeclaredAnnotation(Property.class).name());
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
