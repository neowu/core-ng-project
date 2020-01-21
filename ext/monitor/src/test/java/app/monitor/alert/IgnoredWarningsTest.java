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
class IgnoredWarningsTest {
    private IgnoredWarnings ignoredWarnings;

    @BeforeEach
    void createIgnoredWarnings() {
        var config = new AlertConfig();
        config.ignoreWarnings = List.of(
                ignoreWarnings(List.of("website", "backoffice"), List.of("PATH_NOT_FOUND")),
                ignoreWarnings(List.of("frontend-website"), List.of("API_ERROR_401", "FORBIDDEN"))
        );
        ignoredWarnings = new IgnoredWarnings(config);
    }

    @Test
    void ignore() {
        assertThat(ignoredWarnings.ignore(alert("website", "PATH_NOT_FOUND"))).isTrue();
        assertThat(ignoredWarnings.ignore(alert("frontend-website", "FORBIDDEN"))).isTrue();
        assertThat(ignoredWarnings.ignore(alert("customer-service", "FORBIDDEN"))).isFalse();
        assertThat(ignoredWarnings.ignore(alert("website", "METHOD_NOT_ALLOWED"))).isFalse();
    }

    private Alert alert(String app, String errorCode) {
        Alert alert = new Alert();
        alert.severity = Severity.WARN;
        alert.app = app;
        alert.errorCode = errorCode;
        return alert;
    }

    private AlertConfig.IgnoreWarnings ignoreWarnings(List<String> apps, List<String> errorCodes) {
        var warnings = new AlertConfig.IgnoreWarnings();
        warnings.apps = apps;
        warnings.errorCodes = errorCodes;
        return warnings;
    }
}
