package core.framework.impl.web.bean;

import core.framework.api.util.Maps;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
public final class QueryParamBeanMappers {
    private final Map<Type, QueryParamBeanMapper<?>> mappers = Maps.newConcurrentHashMap();

    public <T> Map<String, String> toParams(T bean) {
        return mapper(bean.getClass()).toParams(bean);
    }

    public <T> T fromParams(Type beanType, Map<String, String> params) {
        QueryParamBeanMapper<T> mapper = mapper(beanType);
        return mapper.fromParams(params);
    }

    public void registerBeanType(Type beanType) {
        mapper(beanType);
    }

    @SuppressWarnings("unchecked")
    private <T> QueryParamBeanMapper<T> mapper(Type beanType) {
        return (QueryParamBeanMapper<T>) mappers.computeIfAbsent(beanType, key -> {
            QueryParamBeanMapperBuilder<T> builder = new QueryParamBeanMapperBuilder<T>((Class<T>) beanType);
            return builder.build();
        });
    }
}
