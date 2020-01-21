package app.monitor.action;

import app.monitor.ActionAlertConfig;
import core.framework.log.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ericchung
 */
class ActionAlertServiceTest {
    private ActionAlertService service;

    @BeforeEach
    void createActionAlertService() {
        var config = new ActionAlertConfig();
        config.criticalErrors = List.of("CRITICAL_ERROR");
        config.site = "site";
        config.timespanInHours = 4;
        config.kibanaURL = "http://kibana:5601";
        config.channel = new ActionAlertConfig.Channel();
        config.channel.actionError = "actionErrorChannel";
        config.channel.actionWarn = "actionWarnChannel";
        config.channel.eventError = "eventErrorChannel";
        config.channel.eventWarn = "eventWarnChannel";
        service = new ActionAlertService(config);
    }

    @Test
    void docURL() {
        assertThat(service.docURL("test", "test-id")).isEqualTo("http://kibana:5601/app/kibana#/doc/test-pattern/test-*?id=test-id&_g=()");
    }

    @Test
    void alertChannel() {
        var alert = new ActionAlert();
        alert.severity = Severity.ERROR;
        alert.kibanaIndex = "trace";
        assertThat(service.alertChannel(alert)).isEqualTo("actionErrorChannel");
    }

    @Test
    void alertKey() {
        ActionAlert alert = alert(Severity.WARN, "NOT_FOUND");
        assertThat(service.alertKey(alert)).isEqualTo("website/WARN/NOT_FOUND");
    }
    
    private ActionAlert alert(Severity severity, String errorCode) {
        var alert = new ActionAlert();
        alert.app = "website";
        alert.severity = severity;
        alert.errorCode = errorCode;
        return alert;
    }

    @Test
    void check() {
        assertThat(service.check(alert(Severity.ERROR, "CRITICAL_ERROR")).notify).isTrue();

        ActionAlert alert = alert(Severity.ERROR, "ERROR");
        LocalDateTime date = LocalDateTime.now();
        ActionAlertService.Result result = service.check(alert, date);
        assertThat(result).matches(r -> r.notify && r.alertCountSinceLastSent == -1);

        result = service.check(alert, date.plusMinutes(30));
        assertThat(result).matches(r -> !r.notify);

        result = service.check(alert, date.plusHours(4));
        assertThat(result).matches(r -> r.notify && r.alertCountSinceLastSent == 1);
    }
}
