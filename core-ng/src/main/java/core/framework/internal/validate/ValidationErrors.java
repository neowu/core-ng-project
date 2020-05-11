package core.framework.internal.validate;

import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public final class ValidationErrors {
    public Map<String, String> errors;

    public void add(String field, String error, Map<String, String> arguments) {
        if (errors == null) errors = Maps.newLinkedHashMap();
        String errorMessage = message(error, arguments);
        errors.put(field, errorMessage);
    }

    // assume var can only be used once, and there are only limited predefined vars, not considering i18n, just to make default message more friendly for engineers
    String message(String error, Map<String, String> arguments) {
        if (arguments == null) return error;
        var builder = new StringBuilder(error);
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            String var = "{" + entry.getKey() + "}";
            int index = builder.indexOf(var);
            if (index >= 0) {
                builder.replace(index, index + var.length(), entry.getValue());
            }
        }
        return builder.toString();
    }

    public boolean hasError() {
        return errors != null;
    }
}
