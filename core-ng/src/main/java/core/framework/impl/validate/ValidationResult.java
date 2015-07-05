package core.framework.impl.validate;

import core.framework.api.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class ValidationResult {
    public Map<String, String> errors;

    public void addError(String field, String error) {
        if (errors == null) errors = Maps.newHashMap();
        errors.put(field, error);
    }

    public boolean isValid() {
        return errors == null;
    }
}
