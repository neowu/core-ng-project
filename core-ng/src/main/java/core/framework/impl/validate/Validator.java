package core.framework.impl.validate;

import core.framework.impl.log.filter.JSONLogParam;
import core.framework.json.JSON;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void validate(Object bean, boolean partial) {
        if (bean == null) {
            throw new ValidationException(Map.of("bean", "bean must not be null"));
        }

        if (validator != null) { // validator can be null if no validation annotation presents
            var errors = new ValidationErrors();
            validator.validate(bean, errors, partial);
            if (errors.hasError()) {
                // all bean can be validated can be converted to JSON, only log on failure path, not slow down happy path (by toJSON)
                // use debug level to not interfere error_code in action log, and it's logging whole bean json, not as proper error message
                LOGGER.debug("validate, beanClass={}, bean={}, partial={}", bean.getClass().getCanonicalName(), new JSONLogParam(Strings.bytes(JSON.toJSON(bean)), UTF_8), partial);
                throw new ValidationException(errors.errors);
            }
        }
    }
}
