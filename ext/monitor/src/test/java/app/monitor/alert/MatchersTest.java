package app.monitor.alert;

import app.monitor.AlertConfig;
import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MatchersTest {
    @Test
    void matchAppAndErrorCode() {
        var matcher = new Matchers(List.of(
            matcher(List.of("website", "backoffice"), List.of(), null, List.of("PATH_NOT_FOUND")),
            matcher(List.of("frontend-website"), List.of(), null, List.of("API_ERROR_401", "FORBIDDEN")),
            matcher(List.of(), List.of(), null, List.of("VALIDATION_ERROR")), // match all apps if apps is empty
            matcher(List.of(), List.of(), null, List.of("UNAUTHORIZED"))
        ));

        assertThat(matcher.match(alert("website", "PATH_NOT_FOUND", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.match(alert("frontend-website", "FORBIDDEN", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.match(alert("customer-service", "FORBIDDEN", Severity.WARN, "trace"))).isFalse();
        assertThat(matcher.match(alert("website", "METHOD_NOT_ALLOWED", Severity.WARN, "trace"))).isFalse();
        assertThat(matcher.match(alert("customer-service", "VALIDATION_ERROR", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.match(alert("customer-service", "UNAUTHORIZED", Severity.WARN, "trace"))).isTrue();
    }

    @Test
    void matchSeverityAndIndex() {
        var matcher = new Matchers(List.of(
            matcher(List.of(), List.of("event"), Severity.ERROR, List.of()),
            matcher(List.of(), List.of("trace", "stat"), Severity.WARN, List.of()),
            matcher(List.of("website"), List.of("trace"), Severity.ERROR, List.of("PATH_NOT_FOUND"))
        ));

        assertThat(matcher.match(alert("website", "UNAUTHORIZED", Severity.ERROR, "event"))).isTrue();
        assertThat(matcher.match(alert("customer-service", "UNAUTHORIZED", Severity.ERROR, "event"))).isTrue();
        assertThat(matcher.match(alert("website", "UNAUTHORIZED", Severity.ERROR, "trace"))).isFalse();
        assertThat(matcher.match(alert("customer-service", "UNAUTHORIZED", Severity.ERROR, "trace"))).isFalse();

        assertThat(matcher.match(alert("website", "UNAUTHORIZED", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.match(alert("customer-service", "UNAUTHORIZED", Severity.WARN, "trace"))).isTrue();
        assertThat(matcher.match(alert("website", "UNAUTHORIZED", Severity.WARN, "event"))).isFalse();
        assertThat(matcher.match(alert("customer-service", "UNAUTHORIZED", Severity.WARN, "event"))).isFalse();

        assertThat(matcher.match(alert("website", "PATH_NOT_FOUND", Severity.ERROR, "trace"))).isTrue();
        assertThat(matcher.match(alert("customer-service", "PATH_NOT_FOUND", Severity.ERROR, "trace"))).isFalse();
        assertThat(matcher.match(alert("website", "UNAUTHORIZED", Severity.ERROR, "trace"))).isFalse();
    }

    private AlertConfig.Matcher matcher(List<String> apps, List<String> indices, Severity severity, List<String> errorCodes) {
        var matcher = new AlertConfig.Matcher();
        matcher.apps = apps;
        matcher.severity = severity;
        matcher.indices = indices;
        matcher.errorCodes = errorCodes;
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
