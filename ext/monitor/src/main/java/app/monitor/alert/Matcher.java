package app.monitor.alert;

import app.monitor.AlertConfig;
import core.framework.log.Severity;

import java.util.Set;

/**
 * @author neo
 */
public class Matcher {
    final Set<String> indices;
    final Set<String> apps;
    final Severity severity;
    final Set<String> errorCodes;

    public Matcher(AlertConfig.Matcher matcher) {
        this.indices = Set.copyOf(matcher.indices);
        this.apps = Set.copyOf(matcher.apps);
        this.severity = matcher.severity;
        this.errorCodes = Set.copyOf(matcher.errorCodes);
    }

    boolean match(Alert alert) {
        return (severity == null || severity == alert.severity)
               && (indices.isEmpty() || indices.contains(alert.kibanaIndex))
               && (apps.isEmpty() || apps.contains(alert.app))
               && (errorCodes.isEmpty() || errorCodes.contains(alert.errorCode));
    }
}
