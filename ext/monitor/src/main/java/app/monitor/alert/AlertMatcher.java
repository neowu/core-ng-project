package app.monitor.alert;

import app.monitor.AlertConfig;
import core.framework.log.Severity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class AlertMatcher {
    public final List<Rule> rules;

    public AlertMatcher(List<AlertConfig.Matcher> matchers) {
        this.rules = matchers.stream()
                .map(matcher -> new Rule(Set.copyOf(matcher.indices), Set.copyOf(matcher.apps), matcher.severity, Set.copyOf(matcher.errorCodes)))
                .collect(Collectors.toList());
    }

    boolean matches(Alert alert) {
        for (Rule rule : rules) {
            if (rule.matches(alert)) {
                return true;
            }
        }
        return false;
    }

    static class Rule {
        final Set<String> indices;
        final Set<String> apps;
        final Severity severity;
        final Set<String> errorCodes;

        Rule(Set<String> indices, Set<String> apps, Severity severity, Set<String> errorCodes) {
            this.indices = indices;
            this.apps = apps;
            this.severity = severity;
            this.errorCodes = errorCodes;
        }

        boolean matches(Alert alert) {
            return (severity == null || severity == alert.severity)
                    && (indices.isEmpty() || indices.contains(alert.kibanaIndex))
                    && (apps.isEmpty() || apps.contains(alert.app))
                    && (errorCodes.isEmpty() || errorCodes.contains(alert.errorCode));
        }
    }
}
