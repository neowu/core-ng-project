package app.monitor.channel;

import app.monitor.alert.Alert;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPResponse;
import core.framework.log.Severity;
import core.framework.util.Strings;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class SlackClientTest {
    @Mock
    HTTPClient httpClient;
    private SlackClient client;

    @BeforeEach
    void createSlackClient() {
        client = new SlackClient(httpClient, "token");
    }

    @Test
    void request() {
        SlackMessageAPIRequest request = client.request("channel", "message", "color");
        assertThat(request.attachments).hasSize(1)
                .has(new Condition<>(attachment -> "message".equals(attachment.text), "attachment.text should be message"),
                        atIndex(0));
    }

    @Test
    void color() {
        LocalDateTime date = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

        assertThat(client.color(Severity.WARN, date)).isEqualTo("#ff5c33");
        assertThat(client.color(Severity.WARN, date.plusWeeks(1))).isEqualTo("#ff9933");

        assertThat(client.color(Severity.ERROR, date)).isEqualTo("#a30101");
        assertThat(client.color(Severity.ERROR, date.plusWeeks(1))).isEqualTo("#e62a00");
    }

    @Test
    void message() {
        var alert = new Alert();
        alert.id = "id";
        alert.app = "website";
        alert.severity = Severity.WARN;
        alert.errorCode = "ERROR_CODE";
        alert.kibanaIndex = "action";
        alert.kibanaURL = "http://kibana:5601";
        alert.errorMessage = "message";
        alert.site = "site";

        assertThat(client.message(alert, 10)).isEqualTo("""
                *[10]* WARN: *site / website*
                _id: <http://kibana:5601/app/kibana#/doc/action-pattern/action-*?id=id&_g=()|id>
                error_code: *ERROR_CODE*
                message: message
                """);

        alert.host = "host";
        assertThat(client.message(alert, 0)).isEqualTo("""
                WARN: *site / website*
                host: host
                _id: <http://kibana:5601/app/kibana#/doc/action-pattern/action-*?id=id&_g=()|id>
                error_code: *ERROR_CODE*
                message: message
                """);

        alert.action = "action";
        alert.host = null;

        assertThat(client.message(alert, 0)).isEqualTo("""
                WARN: *site / website*
                _id: <http://kibana:5601/app/kibana#/doc/action-pattern/action-*?id=id&_g=()|id>
                action: action
                error_code: *ERROR_CODE*
                message: message
                """);
    }

    @Test
    void send() {
        when(httpClient.execute(any())).thenReturn(new HTTPResponse(200, Map.of(), Strings.bytes("{\"ok\": true}")));

        client.send("channel", "message", "red");
    }
}
