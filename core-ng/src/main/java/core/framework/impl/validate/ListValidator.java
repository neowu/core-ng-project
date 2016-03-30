package core.framework.impl.validate;

import java.util.List;

/**
 * @author neo
 */
class ListValidator implements FieldValidator {
    private final List<FieldValidator> valueValidators;

    ListValidator(List<FieldValidator> valueValidators) {
        this.valueValidators = valueValidators;
    }

    @Override
    public void validate(Object list, ValidationErrors errors, boolean partial) {
        if (list == null) return;

        for (Object value : (List<?>) list) {
            for (FieldValidator valueValidator : valueValidators) {
                valueValidator.validate(value, errors, partial);
            }
        }
    }
}
