package core.framework.impl.validate;

import core.framework.impl.log.filter.JSONLogParam;
import core.framework.internal.json.JSONMapper;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);
    private final BeanValidator validator;

    public Validator(Class<?> beanClass, Function<Field, String> fieldNameProvider) {
        var builder = new BeanValidatorBuilder(beanClass, fieldNameProvider);
        this.validator = builder.build().orElse(null);
    }

    // serialize to json without any extra logic
    private static JSONLogParam param(Object instance) {
        try {
            return new JSONLogParam(Strings.bytes(JSONMapper.OBJECT_MAPPER.writeValueAsString(instance)), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void validate(Object bean, boolean partial) {
        if (bean == null) {
            throw new ValidationException(Map.of("bean", "bean must not be null"));
        }

        if (validator != null) { // validator can be null if no validation annotation presents
            var errors = new ValidationErrors();
            validator.validate(bean, errors, partial);
            if (errors.hasError()) {
                // all validatable beans can be converted to JSON, only log content on failure path, not to slow down happy path
                // use debug level not to interfere error_code in action log
                LOGGER.debug("validate, beanClass={}, bean={}, partial={}", bean.getClass().getCanonicalName(), param(bean), partial);
                throw new ValidationException(errors.errors);
            }
        }
    }
}
