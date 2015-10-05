package core.framework.impl.validate;

import core.framework.api.util.Exceptions;

import java.util.List;

/**
 * @author neo
 */
public class ListValidator implements FieldValidator {
    private final ObjectValidator valueValidator;

    public ListValidator(ObjectValidator valueValidator) {
        this.valueValidator = valueValidator;
    }

    @Override
    public void validate(Object list, ValidationErrors errors, boolean partial) {
        if (list instanceof List) {
            for (Object value : ((List<?>) list)) {
                valueValidator.validate(value, errors, partial);
            }
        } else if (list != null) {
            throw Exceptions.error("value must be list, class={}", list.getClass().getCanonicalName());
        }
    }
}
