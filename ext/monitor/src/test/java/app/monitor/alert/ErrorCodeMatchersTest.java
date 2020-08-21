package app.monitor.alert;

import app.monitor.AlertConfig;
import core.framework.log.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ErrorCodeMatchersTest {
    private ErrorCodeMatchers matchers;
    private ErrorCodeMatchers channelMatchers;

    @BeforeEach
    void createErrorCodeMatchers() {
        matchers = new ErrorCodeMatchers(List.of(
            matcher(List.of("website", "backoffice"), List.of("PATH_NOT_FOUND")),
            matcher(List.of("frontend-website"), List.of("API_ERROR_401", "FORBIDDEN")),
            matcher(List.of(), List.of("VALIDATION_ERROR")), // match all apps if apps is empty
            matcher(List.of(), List.of("UNAUTHORIZED"))
        ));

        channelMatchers = new ErrorCodeMatchers(List.of(
            matcher(List.of(), List.of(), Severity.ERROR, "event"),
            matcher(List.of(), List.of(), Severity.WARN, "trace"),
            matcher(List.of("website"), List.of("PATH_NOT_FOUND"), Severity.ERROR, "trace")
        ));
    }

    @Test
    void matches() {
        assertThat(matchers.matches("website", "PATH_NOT_FOUND")).isTrue();
        assertThat(matchers.matches("frontend-website", "FORBIDDEN")).isTrue();
        assertThat(matchers.matches("customer-service", "FORBIDDEN")).isFalse();
        assertThat(matchers.matches("website", "METHOD_NOT_ALLOWED")).isFalse();
        assertThat(matchers.matches("customer-service", "VALIDATION_ERROR")).isTrue();
        assertThat(matchers.matches("customer-service", "UNAUTHORIZED")).isTrue();
    }

    @Test
    void matchesChannel() {
        assertThat(channelMatchers.matches("website", "UNAUTHORIZED", Severity.ERROR, "event")).isTrue();
        assertThat(channelMatchers.matches("customer-service", "UNAUTHORIZED", Severity.ERROR, "event")).isTrue();
        assertThat(channelMatchers.matches("website", "UNAUTHORIZED", Severity.ERROR, "trace")).isFalse();
        assertThat(channelMatchers.matches("customer-service", "UNAUTHORIZED", Severity.ERROR, "trace")).isFalse();

        assertThat(channelMatchers.matches("website", "UNAUTHORIZED", Severity.WARN, "trace")).isTrue();
        assertThat(channelMatchers.matches("customer-service", "UNAUTHORIZED", Severity.WARN, "trace")).isTrue();
        assertThat(channelMatchers.matches("website", "UNAUTHORIZED", Severity.WARN, "event")).isFalse();
        assertThat(channelMatchers.matches("customer-service", "UNAUTHORIZED", Severity.WARN, "event")).isFalse();

        assertThat(channelMatchers.matches("website", "PATH_NOT_FOUND", Severity.ERROR, "trace")).isTrue();
        assertThat(channelMatchers.matches("customer-service", "PATH_NOT_FOUND", Severity.ERROR, "trace")).isFalse();
        assertThat(channelMatchers.matches("website", "UNAUTHORIZED", Severity.ERROR, "trace")).isFalse();
    }

    private AlertConfig.Matcher matcher(List<String> apps, List<String> errorCodes) {
        var matcher = new AlertConfig.Matcher();
        matcher.apps = apps;
        matcher.errorCodes = errorCodes;
        return matcher;
    }

    private AlertConfig.Matcher matcher(List<String> apps, List<String> errorCodes, Severity severity, String kibanaIndex) {
        var matcher = new AlertConfig.Matcher();
        matcher.apps = apps;
        matcher.errorCodes = errorCodes;
        matcher.severity = severity;
        matcher.kibanaIndex = kibanaIndex;
        return matcher;
    }
}
