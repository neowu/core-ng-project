package core.framework.impl.web.bean;

import core.framework.api.util.Maps;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
public final class RequestBeanMapper {
    private final Map<Type, QueryParamMapper<?>> mappers = Maps.newConcurrentHashMap();

    public <T> Map<String, String> toParams(T bean) {
        QueryParamMapper<T> mapper = registerQueryParamBeanType(bean.getClass());
        return mapper.toParams(bean);
    }

    public <T> T fromParams(Type beanType, Map<String, String> params) {
        QueryParamMapper<T> mapper = registerQueryParamBeanType(beanType);
        return mapper.fromParams(params);
    }

    @SuppressWarnings("unchecked")
    public <T> QueryParamMapper<T> registerQueryParamBeanType(Type beanType) {
        return (QueryParamMapper<T>) mappers.computeIfAbsent(beanType, key -> {
            QueryParamMapperBuilder<T> builder = new QueryParamMapperBuilder<T>((Class<T>) beanType);
            return builder.build();
        });
    }
}
