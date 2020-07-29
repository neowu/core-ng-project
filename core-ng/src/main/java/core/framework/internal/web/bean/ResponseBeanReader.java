package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.json.JSONReader;
import core.framework.internal.reflect.GenericTypes;
import core.framework.internal.validate.Validator;
import core.framework.internal.web.service.ErrorResponse;
import core.framework.util.Maps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class ResponseBeanReader {   // used by webservice client
    private final Map<Class<?>, Context<?>> context = Maps.newHashMap();

    public ResponseBeanReader() {
        context.put(ErrorResponse.class, new Context<>(ErrorResponse.class));   // webservice client doesn't need AJAXErrorResponse
    }

    public void register(Type responseType, BeanClassValidator validator) {
        Class<?> beanClass = ContextHelper.responseBeanClass(responseType);

        if (!context.containsKey(beanClass)) {
            validator.validate(beanClass);
            context.put(beanClass, new Context<>(beanClass));
        }
    }

    public Object fromJSON(Type responseType, byte[] body) throws IOException {
        if (void.class == responseType) return null;

        Class<?> beanClass = ContextHelper.responseBeanClass(responseType);
        Context<Object> context = ContextHelper.context(this.context, beanClass);
        Object bean = context.reader.fromJSON(body);
        if (GenericTypes.isOptional(responseType)) {
            if (bean == null) return Optional.empty();
            context.validator.validate(bean, false);
            return Optional.of(bean);
        } else {
            context.validator.validate(bean, false);
            return bean;
        }
    }

    private static class Context<T> {
        final JSONReader<T> reader;
        final Validator<T> validator;

        Context(Class<T> beanClass) {
            reader = JSONReader.of(beanClass);
            validator = Validator.of(beanClass);
        }
    }
}
