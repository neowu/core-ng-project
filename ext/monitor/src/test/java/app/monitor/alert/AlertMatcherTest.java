package app.monitor.alert;

import app.monitor.AlertConfig;
import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class AlertMatcherTest {
    @Test
    void matchAppAndErrorCode() {
        var matcher = new AlertMatcher(List.of(
                matcher(List.of("website", "backoffice"), List.of("PATH_NOT_FOUND"), null, List.of()),
                matcher(List.of("frontend-website"), List.of("API_ERROR_401", "FORBIDDEN"), null, List.of()),
                matcher(List.of(), List.of("VALIDATION_ERROR"), null, List.of()), // match all apps if apps is empty
                matcher(List.of(), List.of("UNAUTHORIZED"), null, List.of())
        ));

        assertThat(matcher.matches(alert("website", "PATH_NOT_FOUND", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.matches(alert("frontend-website", "FORBIDDEN", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.matches(alert("customer-service", "FORBIDDEN", Severity.WARN, "trace"))).isFalse();
        assertThat(matcher.matches(alert("website", "METHOD_NOT_ALLOWED", Severity.WARN, "trace"))).isFalse();
        assertThat(matcher.matches(alert("customer-service", "VALIDATION_ERROR", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.matches(alert("customer-service", "UNAUTHORIZED", Severity.WARN, "trace"))).isTrue();
    }

    @Test
    void matchesSeverityAndIndex() {
        var matcher = new AlertMatcher(List.of(
                matcher(List.of(), List.of(), Severity.ERROR, List.of("event")),
                matcher(List.of(), List.of(), Severity.WARN, List.of("trace", "stat")),
                matcher(List.of("website"), List.of("PATH_NOT_FOUND"), Severity.ERROR, List.of("trace"))
        ));

        assertThat(matcher.matches(alert("website", "UNAUTHORIZED", Severity.ERROR, "event"))).isTrue();
        assertThat(matcher.matches(alert("customer-service", "UNAUTHORIZED", Severity.ERROR, "event"))).isTrue();
        assertThat(matcher.matches(alert("website", "UNAUTHORIZED", Severity.ERROR, "trace"))).isFalse();
        assertThat(matcher.matches(alert("customer-service", "UNAUTHORIZED", Severity.ERROR, "trace"))).isFalse();

        assertThat(matcher.matches(alert("website", "UNAUTHORIZED", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.matches(alert("customer-service", "UNAUTHORIZED", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.matches(alert("website", "UNAUTHORIZED", Severity.WARN, "event"))).isFalse();
        assertThat(matcher.matches(alert("customer-service", "UNAUTHORIZED", Severity.WARN, "event"))).isFalse();

        assertThat(matcher.matches(alert("website", "PATH_NOT_FOUND", Severity.ERROR, "trace"))).isTrue();
        assertThat(matcher.matches(alert("customer-service", "PATH_NOT_FOUND", Severity.ERROR, "trace"))).isFalse();
        assertThat(matcher.matches(alert("website", "UNAUTHORIZED", Severity.ERROR, "trace"))).isFalse();
    }

    @Test
    void matchesExcludeErrorCodes() {
        var rule = new AlertConfig.Matcher();
        rule.excludeErrorCodes = List.of("ERROR_CODE_FOR_OTHER_TEAM");
        var matcher = new AlertMatcher(List.of(rule));

        assertThat(matcher.matches(alert("website", "ERROR_CODE_FOR_OTHER_TEAM", Severity.ERROR, "trace"))).isFalse();
        assertThat(matcher.matches(alert("website", "UNAUTHORIZED", Severity.ERROR, "trace"))).isTrue();
    }

    private AlertConfig.Matcher matcher(List<String> apps, List<String> errorCodes, Severity severity, List<String> indices) {
        var matcher = new AlertConfig.Matcher();
        matcher.apps = apps;
        matcher.errorCodes = errorCodes;
        matcher.severity = severity;
        matcher.indices = indices;
        return matcher;
    }

    private Alert alert(String app, String errorCode, Severity severity, String index) {
        var alert = new Alert();
        alert.app = app;
        alert.errorCode = errorCode;
        alert.severity = severity;
        alert.kibanaIndex = index;
        return alert;
    }
}
