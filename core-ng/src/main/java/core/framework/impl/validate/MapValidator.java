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
    public void validate(Object map, ValidationErrors errors, boolean partial) {
        if (map instanceof Map) {
            ((Map<?, ?>) map).forEach((key, value) -> valueValidator.validate(value, errors, partial));
        } else if (map != null) {
            throw Exceptions.error("expected map, actualClass={}", map.getClass().getCanonicalName());
        }
    }
}
