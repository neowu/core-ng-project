package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONWriter;
import core.framework.internal.validate.Validator;
import core.framework.internal.web.service.ErrorResponse;
import core.framework.internal.web.service.InternalErrorResponse;
import core.framework.util.Maps;
import core.framework.util.Strings;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class ResponseBeanWriter {   // used by controller and web service
    private final Map<Class<?>, Context<?>> context = Maps.newHashMap();

    public ResponseBeanWriter() {
        context.put(InternalErrorResponse.class, new Context<>(InternalErrorResponse.class));
        context.put(ErrorResponse.class, new Context<>(ErrorResponse.class));
    }

    public void register(Type responseType, BeanClassValidator validator) {
        Class<?> beanClass = ResponseBeanReader.responseBeanClass(responseType);

        if (!context.containsKey(beanClass)) {
            validator.validate(beanClass);
            context.put(beanClass, new Context<>(beanClass));
        }
    }

    public boolean contains(Class<?> beanClass) {
        return context.containsKey(beanClass);
    }

    public byte[] toJSON(Object bean) {
        if (bean instanceof Optional<?> optional) {  // only support Optional<T> as response bean type
            if (optional.isEmpty()) return Strings.bytes("null");
            Object value = optional.get();
            Context<Object> context = context(this.context, value.getClass());
            context.validator.validate(value, false);
            return context.writer.toJSON(value);
        } else {
            Context<Object> context = context(this.context, bean.getClass());
            context.validator.validate(bean, false);
            return context.writer.toJSON(bean);
        }
    }

    private <T> T context(Map<Class<?>, ?> context, Class<?> beanClass) {
        @SuppressWarnings("unchecked")
        T result = (T) context.get(beanClass);
        if (result == null) {
            if (beanClass.getPackageName().startsWith("java")) {   // provide better error message for developer, rather than return class is not registered message
                throw new Error("bean class must not be java built-in class, class=" + beanClass.getCanonicalName());
            }
            throw new Error("bean class is not registered, please use http().bean() to register, class=" + beanClass.getCanonicalName());
        }
        return result;
    }

    private static class Context<T> {
        final JSONWriter<T> writer;
        final Validator<T> validator;

        Context(Class<T> beanClass) {
            writer = JSONMapper.writer(beanClass);
            validator = Validator.of(beanClass);
        }
    }
}
