package app.monitor.alert;

import app.monitor.AlertConfig;
import app.monitor.slack.SlackClient;
import core.framework.log.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author ericchung
 */
@ExtendWith(MockitoExtension.class)
class AlertServiceTest {
    private AlertService service;
    @Mock
    private SlackClient slackClient;

    @BeforeEach
    void createActionAlertService() {
        var config = new AlertConfig();
        config.ignoreErrors = List.of(matcher(List.of("website"), List.of("PATH_NOT_FOUND"), Severity.WARN, null));
        config.criticalErrors = List.of(matcher(null, List.of("CRITICAL_ERROR", "SLOW_SQL"), Severity.ERROR, null));
        config.site = "site";
        config.timespanInHours = 4;
        config.kibanaURL = "http://kibana:5601";

        config.channels = Map.of("actionWarnChannel", matcher(null, null, Severity.WARN, List.of("trace", "stat")),
                "actionErrorChannel", matcher(null, null, Severity.ERROR, List.of("trace", "stat")),
                "eventWarnChannel", matcher(null, null, Severity.WARN, List.of("event")),
                "eventErrorChannel", matcher(null, null, Severity.ERROR, List.of("event")),
                "productChannel", matcher(List.of("website"), List.of("PRODUCT_ERROR"), null, null));

        service = new AlertService(config);
        service.slackClient = slackClient;
    }

    private AlertConfig.Matcher matcher(List<String> apps, List<String> errorCodes, Severity severity, List<String> indices) {
        var matcher = new AlertConfig.Matcher();
        if (apps != null) matcher.apps = apps;
        if (errorCodes != null) matcher.errorCodes = errorCodes;
        matcher.severity = severity;
        if (indices != null) matcher.indices = indices;
        return matcher;
    }

    @Test
    void docURL() {
        assertThat(service.docURL("test", "test-id")).isEqualTo("http://kibana:5601/app/kibana#/doc/test-pattern/test-*?id=test-id&_g=()");
    }

    @Test
    void alertKey() {
        Alert alert = alert(Severity.WARN, "NOT_FOUND", "trace");
        alert.action = "action";
        assertThat(service.alertKey(alert)).isEqualTo("website/action/WARN/NOT_FOUND");
    }

    @Test
    void processWithIgnoredWarning() {
        service.process(alert(Severity.WARN, "PATH_NOT_FOUND", "trace"));
        verifyNoInteractions(slackClient);
    }

    @Test
    void processWithError() {
        service.process(alert(Severity.ERROR, "java.lang.NullPointerException", "trace"));
        verify(slackClient).send(eq("actionErrorChannel"), anyString(), anyString());
    }

    @Test
    void processWithProductError() {
        service.process(alert(Severity.ERROR, "PRODUCT_ERROR", "trace"));
        verify(slackClient).send(eq("actionErrorChannel"), anyString(), anyString());
        verify(slackClient).send(eq("productChannel"), anyString(), anyString());
    }

    @Test
    void processWithEventError() {
        service.process(alert(Severity.ERROR, "EVENT_ERROR", "event"));
        verify(slackClient).send(eq("eventErrorChannel"), anyString(), anyString());
    }

    @Test
    void check() {
        assertThat(service.check(alert(Severity.ERROR, "CRITICAL_ERROR", "trace"), LocalDateTime.now()).notify).isTrue();
        assertThat(service.check(alert(Severity.WARN, "SLOW_SQL", "trace"), LocalDateTime.now()).notify).isTrue();

        Alert alert = alert(Severity.ERROR, "ERROR", "trace");
        LocalDateTime date = LocalDateTime.now();
        AlertService.Result result = service.check(alert, date);
        assertThat(result).matches(r -> r.notify && r.alertCountSinceLastSent == -1);

        result = service.check(alert, date.plusMinutes(30));
        assertThat(result).matches(r -> !r.notify);

        result = service.check(alert, date.plusHours(4));
        assertThat(result).matches(r -> r.notify && r.alertCountSinceLastSent == 1);
    }

    @Test
    void color() {
        LocalDateTime date = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

        assertThat(service.color(Severity.WARN, date)).isEqualTo("#ff5c33");
        assertThat(service.color(Severity.WARN, date.plusWeeks(1))).isEqualTo("#ff9933");

        assertThat(service.color(Severity.ERROR, date)).isEqualTo("#a30101");
        assertThat(service.color(Severity.ERROR, date.plusWeeks(1))).isEqualTo("#e62a00");
    }

    @Test
    void message() {
        Alert alert = alert(Severity.WARN, "ERROR_CODE", "trace");
        alert.id = "id";
        alert.errorMessage = "message";
        alert.kibanaIndex = "action";

        assertThat(service.message(alert, 10)).isEqualTo("*[10]* WARN: *site / website*\n"
                + "_id: <http://kibana:5601/app/kibana#/doc/action-pattern/action-*?id=id&_g=()|id>\n"
                + "error_code: *ERROR_CODE*\n"
                + "message: message\n");

        alert.host = "host";
        assertThat(service.message(alert, 0)).isEqualTo("WARN: *site / website*\n"
                + "host: host\n"
                + "_id: <http://kibana:5601/app/kibana#/doc/action-pattern/action-*?id=id&_g=()|id>\n"
                + "error_code: *ERROR_CODE*\n"
                + "message: message\n");

        alert.action = "action";
        alert.host = null;

        assertThat(service.message(alert, 0)).isEqualTo("WARN: *site / website*\n"
                + "_id: <http://kibana:5601/app/kibana#/doc/action-pattern/action-*?id=id&_g=()|id>\n"
                + "action: action\n"
                + "error_code: *ERROR_CODE*\n"
                + "message: message\n");
    }

    private Alert alert(Severity severity, String errorCode, String index) {
        var alert = new Alert();
        alert.app = "website";
        alert.severity = severity;
        alert.errorCode = errorCode;
        alert.kibanaIndex = index;
        return alert;
    }
}
