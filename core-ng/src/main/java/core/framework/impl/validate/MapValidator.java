package core.framework.impl.validate;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class MapValidator implements FieldValidator {
    private final List<FieldValidator> valueValidators;

    MapValidator(List<FieldValidator> valueValidators) {
        this.valueValidators = valueValidators;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (value == null) return;
        @SuppressWarnings("unchecked")
        Map<String, ?> map = (Map<String, ?>) value;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            for (FieldValidator valueValidator : valueValidators) {
                valueValidator.validate(entry.getValue(), errors, partial);
            }
        }
    }
}
