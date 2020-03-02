package app.monitor.alert;

import app.monitor.AlertConfig;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class ErrorCodeMatchers {
    public final List<Matcher> matchers;

    public ErrorCodeMatchers(List<AlertConfig.Matcher> matchers) {
        this.matchers = matchers.stream()
                                .map(warning -> new Matcher(Set.copyOf(warning.apps), Set.copyOf(warning.errorCodes)))
                                .collect(Collectors.toList());
    }

    boolean matches(String app, String errorCode) {
        for (Matcher matcher : matchers) {
            if (matcher.matches(app, errorCode)) {
                return true;
            }
        }
        return false;
    }

    static class Matcher {
        final Set<String> apps;
        final Set<String> errorCodes;

        Matcher(Set<String> apps, Set<String> errorCodes) {
            this.apps = apps;
            this.errorCodes = errorCodes;
        }

        boolean matches(String app, String errorCode) {
            return (apps.isEmpty() || apps.contains(app))
                    && errorCodes.contains(errorCode);
        }
    }
}
