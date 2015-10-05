package core.framework.impl.validate;

import core.framework.api.util.Exceptions;

import java.util.Map;

/**
 * @author neo
 */
public class MapValidator implements FieldValidator {
    private final ObjectValidator valueValidator;

    public MapValidator(ObjectValidator valueValidator) {
        this.valueValidator = valueValidator;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) value;
            for (Map.Entry<String, ?> entry : ((Map<String, ?>) map).entrySet()) {
                valueValidator.validate(entry.getValue(), errors, partial);
            }
        } else if (value != null) {
            throw Exceptions.error("value must be map, class={}", value.getClass().getCanonicalName());
        }
    }
}
