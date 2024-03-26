package app.monitor.api;

import core.framework.util.Strings;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author neo
 */
public class APIWarnings {
    final Set<String> warnings = new LinkedHashSet<>();
    final Set<String> errors = new LinkedHashSet<>();

    void add(String pattern, Object... params) {
        add(false, pattern, params);
    }

    void add(boolean warning, String pattern, Object... params) {
        String message = Strings.format(pattern, params);
        if (warning) {
            warnings.add(message);
        } else {
            errors.add(message);
        }
    }

    void removeDuplicateWarnings() {
        warnings.removeAll(errors);   // remove warnings if there is same error, e.g. one change is referred by both request/response bean
    }

    public String result() {
        if (!errors.isEmpty()) return "ERROR";
        if (!warnings.isEmpty()) return "WARN";
        return null;
    }

    public String errorMessage() {
        var builder = new StringBuilder(64);
        if (!errors.isEmpty()) {
            builder.append("*incompatible changes*\n");
            errors.forEach(error -> builder.append("* ").append(error).append('\n'));
        }
        if (!warnings.isEmpty()) {
            builder.append("*compatible changes*\n");
            warnings.forEach(warning -> builder.append("* ").append(warning).append('\n'));
        }
        return builder.toString();
    }
}
