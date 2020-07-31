package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.validate.Validator;
import core.framework.util.Maps;

import java.io.IOException;
import java.util.Map;

/**
 * @author neo
 */
public class RequestBeanReader {    // used by controller and web service
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
        QueryParamContext<T> context = ContextHelper.context(queryParamContext, beanClass);
        T bean = context.reader.fromParams(params);
        context.validator.validate(bean, false);
        return bean;
    }

    public <T> T fromJSON(Class<T> beanClass, byte[] body) throws IOException {
        BeanContext<T> context = ContextHelper.context(beanContext, beanClass);
        T bean = context.reader.fromJSON(body);
        context.validator.validate(bean, false);
        return bean;
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
