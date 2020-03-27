package core.log.web;

import core.framework.inject.Inject;
import core.framework.json.Bean;
import core.framework.log.message.EventMessage;
import core.framework.util.Strings;
import core.framework.web.CookieSpec;
import core.framework.web.Request;
import core.framework.web.exception.ForbiddenException;
import core.log.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static core.log.web.SendEventRequest.Event;
import static core.log.web.SendEventRequest.Result;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class EventControllerTest extends IntegrationTest {
    @Inject
    SendEventRequestValidator validator;
    private Request request;

    @BeforeEach
    void prepare() {
        request = mock(Request.class);
    }

    @Test
    void checkOriginWithWildcard() {
        var controller = new EventController(List.of("*"), null);
        controller.checkOrigin("https://localhost");
    }

    @Test
    void checkOrigin() {
        var controller = new EventController(List.of("localhost", "example.com"), null);
        controller.checkOrigin("https://localhost");
        controller.checkOrigin("https://api.example.com");

        assertThatThrownBy(() -> controller.checkOrigin("https://example1.com"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("access denied");
    }

    @Test
    void options() {
        when(request.header("Origin")).thenReturn(Optional.empty());
        var controller = new EventController(List.of("*"), null);
        assertThatThrownBy(() -> controller.options(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void allowPOSTWithCORS() {
        when(request.header("Origin")).thenReturn(Optional.of("localhost"));
        var controller = new EventController(List.of("*"), null);
        assertThat(controller.options(request).header("Access-Control-Allow-Methods"))
                .hasValueSatisfying(methods -> assertThat(methods).contains("POST"));
    }

    @Test
    void message() {
        var event = new Event();
        event.date = ZonedDateTime.now().minusHours(1);
        event.result = Result.WARN;
        event.errorCode = "NOT_FOUND";
        event.errorMessage = "not found";
        event.context.put("path", "/path");
        event.info.put("stackTrace", "trace");
        event.elapsedTime = 100L;

        var controller = new EventController(List.of(), null);
        Instant now = event.date.plusHours(1).toInstant();
        EventMessage message = controller.message(event, "test", now);

        assertThat(message.id).isNotNull();
        assertThat(message.date).isEqualTo(event.date.toInstant());
        assertThat(message.receivedTime).isEqualTo(now);
        assertThat(message.result).isEqualTo("WARN");
        assertThat(message.errorCode).isEqualTo(event.errorCode);
        assertThat(message.errorMessage).isEqualTo(event.errorMessage);
        assertThat(message.action).isNull();
        assertThat(message.context).isEqualTo(event.context);
        assertThat(message.info).isEqualTo(event.info);
        assertThat(message.elapsed).isEqualTo(event.elapsedTime);
        assertThat(message.app).isEqualTo("test");
    }

    @Test
    void sendEventRequest() {
        var event = new Event();
        event.date = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));
        event.result = Result.OK;
        event.action = "action";
        event.elapsedTime = 10L;
        var sendEventRequest = new SendEventRequest();
        sendEventRequest.events.add(event);

        when(request.body()).thenReturn(Optional.of(Strings.bytes(Bean.toJSON(sendEventRequest))));

        var controller = new EventController(List.of(), null);
        controller.validator = validator;
        SendEventRequest parsedSendEventRequest = controller.sendEventRequest(request);

        assertThat(parsedSendEventRequest).usingRecursiveComparison().isEqualTo(sendEventRequest);
    }

    @Test
    void cookies() {
        var controller = new EventController(List.of(), List.of("visitor_id"));

        when(request.cookie(new CookieSpec("visitor_id"))).thenReturn(Optional.empty());
        List<Cookie> cookies = controller.cookies(request);
        assertThat(cookies).hasSize(0);

        when(request.cookie(new CookieSpec("visitor_id"))).thenReturn(Optional.of("value"));
        cookies = controller.cookies(request);
        assertThat(cookies).hasSize(1);
        Cookie cookie = cookies.get(0);
        assertThat(cookie.name).isEqualTo("visitor_id");
        assertThat(cookie.value).isEqualTo("value");
    }

    @Test
    void addContext() {
        var controller = new EventController(List.of(), null);
        Map<String, String> context = new HashMap<>();
        controller.addContext(context, "agent", List.of(new Cookie("cookie", "value")), "ip");

        assertThat(context).containsKeys("client_ip", "user_agent", "cookie");
    }
}
