package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONWriter;
import core.framework.internal.validate.Validator;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class RequestBeanWriter {    // used by webservice client
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

    public <T> Map<String, String> toParams(Class<T> beanClass, T bean) {
        @SuppressWarnings("unchecked")
        QueryParamContext<T> context = (QueryParamContext<T>) queryParamContext.get(beanClass);     // query param is registered thru APIConfig
        context.validator.validate(bean, false);
        return context.writer.toParams(bean);
    }

    public <T> byte[] toJSON(Class<T> beanClass, T bean) {
        @SuppressWarnings("unchecked")
        BeanContext<T> context = (BeanContext<T>) beanContext.get(beanClass);       // bean is registered thru APIConfig
        context.validator.validate(bean, false);
        return context.writer.toJSON(bean);
    }

    private static class QueryParamContext<T> {
        final QueryParamWriter<T> writer;
        final Validator<T> validator;

        QueryParamContext(Class<T> beanClass) {
            writer = new QueryParamWriterBuilder<>(beanClass).build();
            validator = Validator.of(beanClass);
        }
    }

    private static class BeanContext<T> {
        final JSONWriter<T> writer;
        final Validator<T> validator;

        BeanContext(Class<T> beanClass) {
            writer = JSONMapper.writer(beanClass);
            validator = Validator.of(beanClass);
        }
    }
}
