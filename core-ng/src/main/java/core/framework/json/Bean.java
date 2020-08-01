package core.framework.json;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.json.JSONWriter;
import core.framework.internal.validate.Validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public final class Bean {
    private static final Map<Class<?>, Context<?>> CONTEXT = new HashMap<>(); // always requires register beanClass during startup, so not be thread safe

    public static void register(Class<?> beanClass) {
        CONTEXT.compute(beanClass, (key, value) -> {
            if (value != null) throw new Error("bean class is already registered, beanClass=" + key.getCanonicalName());
            new JSONClassValidator(key).validate();
            return new Context<>(beanClass);
        });
    }

    public static <T> T fromJSON(Class<T> beanClass, String json) {
        Context<T> context = context(beanClass);
        try {
            T instance = context.reader.fromJSON(json);
            context.validator.validate(instance, false);
            return instance;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> String toJSON(T bean) {
        Context<T> context = context(bean.getClass());

        context.validator.validate(bean, false);
        return context.writer.toJSONString(bean);
    }

    private static <T> Context<T> context(Class<?> beanClass) {
        @SuppressWarnings("unchecked")
        Context<T> context = (Context<T>) CONTEXT.get(beanClass);
        if (context == null) throw new Error("bean class is not registered, beanClass=" + beanClass.getCanonicalName());
        return context;
    }

    private static class Context<T> {
        final JSONReader<T> reader;
        final JSONWriter<T> writer;
        final Validator<T> validator;

        Context(Class<T> beanClass) {
            reader = JSONMapper.reader(beanClass);
            writer = JSONMapper.writer(beanClass);
            validator = Validator.of(beanClass);
        }
    }
}
