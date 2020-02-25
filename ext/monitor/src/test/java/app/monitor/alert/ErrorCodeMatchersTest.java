package app.monitor.alert;

import app.monitor.AlertConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ErrorCodeMatchersTest {
    private ErrorCodeMatchers matchers;

    @BeforeEach
    void createErrorCodeMatchers() {
        matchers = new ErrorCodeMatchers(List.of(
                matcher(List.of("website", "backoffice"), List.of("PATH_NOT_FOUND")),
                matcher(List.of("frontend-website"), List.of("API_ERROR_401", "FORBIDDEN")),
                matcher(List.of(), List.of("VALIDATION_ERROR")), // match all apps if apps is empty
                matcher(List.of(), List.of("UNAUTHORIZED"))
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

    private AlertConfig.Matcher matcher(List<String> apps, List<String> errorCodes) {
        var matcher = new AlertConfig.Matcher();
        matcher.apps = apps;
        matcher.errorCodes = errorCodes;
        return matcher;
    }
}
