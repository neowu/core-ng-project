package app.monitor.alert;

import app.monitor.AlertConfig;
import app.monitor.channel.ChannelManager;
import core.framework.log.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    private ChannelManager channelManager;

    @BeforeEach
    void createActionAlertService() {
        var config = new AlertConfig();
        config.ignoreErrors = List.of(matcher(List.of("website"), List.of("PATH_NOT_FOUND"), Severity.WARN, null));
        config.criticalErrors = List.of(matcher(null, List.of("CRITICAL_ERROR", "SLOW_SQL"), Severity.ERROR, null));
        config.site = "site";
        config.timespanInHours = 4;
        config.kibanaURL = "http://kibana:5601";

        config.notifications = List.of(notification("actionWarnChannel", null, null, Severity.WARN, List.of("trace", "stat")),
            notification("actionErrorChannel", null, null, Severity.ERROR, List.of("trace", "stat")),
            notification("eventWarnChannel", null, null, Severity.WARN, List.of("event")),
            notification("eventErrorChannel", null, null, Severity.ERROR, List.of("event")),
            notification("productChannel", List.of("website"), List.of("PRODUCT_ERROR"), null, null));

        service = new AlertService(config);
        service.channelManager = channelManager;
    }

    private AlertConfig.Notification notification(String channel, List<String> apps, List<String> errorCodes, Severity severity, List<String> indices) {
        var notification = new AlertConfig.Notification();
        notification.channel = channel;
        notification.matcher = matcher(apps, errorCodes, severity, indices);
        return notification;
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
    void alertKey() {
        Alert alert = alert(Severity.WARN, "NOT_FOUND", "trace");
        alert.action = "action";
        assertThat(service.alertKey(alert)).isEqualTo("website/action/WARN/NOT_FOUND");
    }

    @Test
    void processWithIgnoredWarning() {
        service.process(alert(Severity.WARN, "PATH_NOT_FOUND", "trace"));
        verifyNoInteractions(channelManager);
    }

    @Test
    void processWithError() {
        Alert alert = alert(Severity.ERROR, "java.lang.NullPointerException", "trace");
        service.process(alert);
        verify(channelManager).notify(eq("actionErrorChannel"), eq(alert), eq(-1));
    }

    @Test
    void processWithProductError() {
        Alert alert = alert(Severity.ERROR, "PRODUCT_ERROR", "trace");
        service.process(alert);
        verify(channelManager).notify(eq("actionErrorChannel"), eq(alert), eq(-1));
        verify(channelManager).notify(eq("productChannel"), eq(alert), eq(-1));
    }

    @Test
    void processWithEventError() {
        Alert alert = alert(Severity.ERROR, "EVENT_ERROR", "event");
        service.process(alert);
        verify(channelManager).notify(eq("eventErrorChannel"), eq(alert), eq(-1));
    }

    @Test
    void check() {
        Alert alert = alert(Severity.ERROR, "ERROR", "trace");
        AlertService.Result result = service.check(alert);
        assertThat(result).matches(r -> r.notify && r.alertCountSinceLastSent == -1);

        alert.date = alert.date.plusMinutes(30);
        result = service.check(alert);
        assertThat(result).matches(r -> !r.notify);

        alert.date = alert.date.plusMinutes(210);
        result = service.check(alert);
        assertThat(result).matches(r -> r.notify && r.alertCountSinceLastSent == 1);
    }

    @Test
    void checkWithCriticalError() {
        Alert alert = alert(Severity.ERROR, "CRITICAL_ERROR", "trace");
        AlertService.Result result = service.check(alert);
        assertThat(result).matches(r -> r.notify && r.alertCountSinceLastSent == -1);

        alert.date = alert.date.plusSeconds(30);
        result = service.check(alert);
        assertThat(result).matches(r -> !r.notify);

        alert.date = alert.date.plusMinutes(1);
        result = service.check(alert);
        assertThat(result).matches(r -> r.notify && r.alertCountSinceLastSent == 1);
    }

    private Alert alert(Severity severity, String errorCode, String index) {
        var alert = new Alert();
        alert.date = LocalDateTime.now();
        alert.app = "website";
        alert.severity = severity;
        alert.errorCode = errorCode;
        alert.kibanaIndex = index;
        return alert;
    }
}
