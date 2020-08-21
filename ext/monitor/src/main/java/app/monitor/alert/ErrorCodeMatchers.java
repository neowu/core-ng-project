package app.monitor.alert;

import app.monitor.AlertConfig;
import core.framework.log.Severity;
import core.framework.util.Strings;

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
                                .map(warning -> new Matcher(Set.copyOf(warning.apps), Set.copyOf(warning.errorCodes), warning.severity, warning.kibanaIndex))
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

    boolean matches(String app, String errorCode, Severity severity, String kibanaIndex) {
        for (Matcher matcher : matchers) {
            if (matcher.matches(app, errorCode, severity, kibanaIndex)) {
                return true;
            }
        }
        return false;
    }

    static class Matcher {
        final Set<String> apps;
        final Set<String> errorCodes;
        final Severity severity;
        final String kibanaIndex;

        Matcher(Set<String> apps, Set<String> errorCodes, Severity severity, String kibanaIndex) {
            this.apps = apps;
            this.errorCodes = errorCodes;
            this.severity = severity;
            this.kibanaIndex = kibanaIndex;
        }

        boolean matches(String app, String errorCode) {
            return (apps.isEmpty() || apps.contains(app))
                    && errorCodes.contains(errorCode);
        }

        boolean matches(String app, String errorCode, Severity alertSeverity, String alertKibanaIndex) {
            return (severity == null || severity == alertSeverity)
                && (apps.isEmpty() || apps.contains(app))
                && (errorCodes.isEmpty() || errorCodes.contains(errorCode))
                && (Strings.isBlank(kibanaIndex) || kibanaIndex.equals(alertKibanaIndex));
        }
    }
}
