package core.framework.impl.validate;

import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public final class ValidationErrors {
    public Map<String, String> errors;

    public void add(String field, String error) {
        if (errors == null) errors = Maps.newLinkedHashMap();
        errors.put(field, error);
    }

    public boolean hasError() {
        return errors != null;
    }
}
