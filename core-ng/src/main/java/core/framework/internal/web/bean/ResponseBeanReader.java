package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.reflect.GenericTypes;
import core.framework.internal.validate.Validator;
import core.framework.internal.web.service.InternalErrorResponse;
import core.framework.util.Maps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class ResponseBeanReader {   // used by webservice client
    static Class<?> responseBeanClass(Type responseType) {
        return GenericTypes.isOptional(responseType) ? GenericTypes.optionalValueClass(responseType) : GenericTypes.rawClass(responseType);
    }

    final Map<Class<?>, Context<?>> context = Maps.newHashMap();

    public ResponseBeanReader() {
        context.put(InternalErrorResponse.class, new Context<>(InternalErrorResponse.class));   // webservice client doesn't need ErrorResponse
    }

    public void register(Type responseType, BeanClassValidator validator) {
        Class<?> beanClass = responseBeanClass(responseType);

        if (!context.containsKey(beanClass)) {
            validator.validate(beanClass);
            context.put(beanClass, new Context<>(beanClass));
        }
    }

    public Object fromJSON(Type responseType, byte[] body) throws IOException {
        if (void.class == responseType) return null;

        Class<?> beanClass = responseBeanClass(responseType);
        @SuppressWarnings("unchecked")
        Context<Object> context = (Context<Object>) this.context.get(beanClass);    // response type is registered thru APIConfig
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

    static class Context<T> {
        final JSONReader<T> reader;
        final Validator<T> validator;

        Context(Class<T> beanClass) {
            reader = JSONMapper.reader(beanClass);
            validator = Validator.of(beanClass);
        }
    }
}
