package core.framework.internal.web.bean;

import core.framework.api.web.service.QueryParam;
import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.validate.Validator;
import core.framework.util.Maps;
import core.framework.web.exception.BadRequestException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author neo
 */
public class RequestBeanReader {    // used by controller and web service
    public static boolean isQueryParamBean(Class<?> beanClass) {
        Field[] fields = beanClass.getDeclaredFields();
        if (fields.length == 0) return false;
        return fields[0].isAnnotationPresent(QueryParam.class);
    }

    private final Map<Class<?>, QueryParamContext<?>> queryParamContext = Maps.newHashMap();
    private final Map<Class<?>, BeanContext<?>> beanContext = Maps.newHashMap();

    public <T> void registerQueryParam(Class<T> beanClass, BeanClassNameValidator beanClassNameValidator) {
        if (!queryParamContext.containsKey(beanClass)) {
            new QueryParamClassValidator(beanClass, beanClassNameValidator).validate();
            queryParamContext.put(beanClass, new QueryParamContext<>(beanClass));
        }
    }

    public void registerBean(Class<?> beanClass, BeanClassValidator validator) {
        if (!beanContext.containsKey(beanClass)) {
            validator.validate(beanClass);
            beanContext.put(beanClass, new BeanContext<>(beanClass));
        }
    }

    public boolean containsQueryParam(Class<?> beanClass) {
        return queryParamContext.containsKey(beanClass);
    }

    public boolean containsBean(Class<?> beanClass) {
        return beanContext.containsKey(beanClass);
    }

    public <T> T fromParams(Class<T> beanClass, Map<String, String> params) {
        QueryParamContext<T> context = context(queryParamContext, beanClass, true);
        T bean = context.reader.fromParams(params);
        context.validator.validate(bean, false);
        return bean;
    }

    public <T> T fromJSON(Class<T> beanClass, byte[] body) throws IOException {
        BeanContext<T> context = context(beanContext, beanClass, false);
        T bean = context.reader.fromJSON(body);
        context.validator.validate(bean, false);
        return bean;
    }

    private <T> T context(Map<Class<?>, ?> context, Class<?> beanClass, boolean expectQueryParam) {
        @SuppressWarnings("unchecked")
        T result = (T) context.get(beanClass);
        if (result == null) {
            if (beanClass.getPackageName().startsWith("java")) {   // provide better error message for developer, rather than return class is not registered message
                throw new Error("bean class must not be java built-in class, class=" + beanClass.getCanonicalName());
            }
            if (expectQueryParam != isQueryParamBean(beanClass)) {
                throw new BadRequestException("content-type not supported, class=" + beanClass.getCanonicalName());
            }
            throw new Error("bean class is not registered, please use http().bean() to register, class=" + beanClass.getCanonicalName());
        }
        return result;
    }

    private static class QueryParamContext<T> {
        final QueryParamReader<T> reader;
        final Validator<T> validator;

        QueryParamContext(Class<T> beanClass) {
            reader = new QueryParamReaderBuilder<>(beanClass).build();
            validator = Validator.of(beanClass);
        }
    }

    private static class BeanContext<T> {
        final JSONReader<T> reader;
        final Validator<T> validator;

        BeanContext(Class<T> beanClass) {
            reader = JSONMapper.reader(beanClass);
            validator = Validator.of(beanClass);
        }
    }
}
