package core.framework.impl.validate;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
class OptionalValidator implements FieldValidator {
    private final List<FieldValidator> valueValidators;

    OptionalValidator(List<FieldValidator> valueValidators) {
        this.valueValidators = valueValidators;
    }

    @Override
    public void validate(Object optional, ValidationErrors errors, boolean partial) {
        if (optional == null) return;

        ((Optional<?>) optional).ifPresent(value -> {
            for (FieldValidator valueValidator : valueValidators) {
                valueValidator.validate(value, errors, partial);
            }
        });
    }
}
