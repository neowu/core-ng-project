package core.framework.internal.validate;

import core.framework.internal.log.filter.BytesLogParam;
import core.framework.json.JSON;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public final class Validator<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);
    private static Map<Class<?>, Validator<?>> validators = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Validator<T> of(Class<T> beanClass) {
        // can only be used during config time within module, App will run cleanup after startup
        return (Validator<T>) validators.computeIfAbsent(beanClass, Validator::new);
    }

    public static void cleanup() {
        validators = null;
    }

    @Nullable
    private final BeanValidator validator;

    private Validator(Class<T> beanClass) {
        var builder = new BeanValidatorBuilder(beanClass);
        this.validator = builder.build();
    }

    public void validate(T bean, boolean partial) {
        Map<String, String> errors = errors(bean, partial);
        if (errors != null) throw new ValidationException(errors);
    }

    // used only internally, for places don't want to catch exception
    public Map<String, String> errors(T bean, boolean partial) {
        if (bean == null) {
            return Map.of("bean", "bean must not be null");
        }

        if (validator != null) { // validator can be null if no validation annotation presents
            var errors = new ValidationErrors();
            validator.validate(bean, errors, partial);
            if (errors.hasError()) {
                // all validatable beans can be converted to JSON, only log content on failure path, not to slow down happy path
                // use debug level not to interfere error_code in action log
                LOGGER.debug("validate, beanClass={}, bean={}, partial={}", bean.getClass().getCanonicalName(),
                        new BytesLogParam(Strings.bytes(JSON.toJSON(bean))),
                        partial);
                return errors.errors;
            }
        }
        return null;
    }
}
