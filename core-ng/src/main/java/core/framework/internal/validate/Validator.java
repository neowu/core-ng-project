package core.framework.internal.validate;

import core.framework.internal.log.filter.JSONLogParam;
import core.framework.json.JSON;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class Validator<T> {
    // TODO: remove soon
    private static final Map<Class<?>, Validator<?>> VALIDATORS = new HashMap<>();    // validators are always created during startup in single thread, it's ok not be thread safe
    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    @SuppressWarnings("unchecked")
    public static <T> Validator<T> of(Class<T> beanClass) {
        return (Validator<T>) VALIDATORS.computeIfAbsent(beanClass, Validator::new);
    }

    @Nullable
    private final BeanValidator validator;

    public Validator(Class<T> beanClass) {
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
                        new JSONLogParam(Strings.bytes(JSON.toJSON(bean)), UTF_8),
                        partial);
                return errors.errors;
            }
        }
        return null;
    }
}
