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
    public void validate(Object list, ValidationErrors errors) {
        if (list instanceof List) {
            ((List<?>) list).forEach(value -> valueValidator.validate(value, errors));
        } else if (list != null) {
            throw Exceptions.error("expected list, actualClass={}", list.getClass().getCanonicalName());
        }
    }
}
