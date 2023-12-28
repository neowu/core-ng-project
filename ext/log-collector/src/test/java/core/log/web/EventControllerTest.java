package core.log.web;

import core.framework.http.ContentType;
import core.framework.http.HTTPHeaders;
import core.framework.json.JSON;
import core.framework.log.message.EventMessage;
import core.framework.util.Strings;
import core.framework.web.CookieSpec;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.ForbiddenException;
import core.log.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static core.log.web.SendEventRequest.Event;
import static core.log.web.SendEventRequest.Result;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class EventControllerTest extends IntegrationTest {
    @Mock
    Request request;

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
    void post() {
        when(request.header("Origin")).thenReturn(Optional.of("localhost"));
        when(request.body()).thenReturn(Optional.empty());
        var controller = new EventController(List.of("localhost"), null);
        Response response = controller.post(request);
        assertThat(response.header("Access-Control-Allow-Origin")).get().isEqualTo("localhost");
        assertThat(response.header("Access-Control-Allow-Credentials")).get().isEqualTo("true");
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
        event.date = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        event.result = Result.OK;
        event.action = "action";
        event.elapsedTime = 10L;
        var sendEventRequest = new SendEventRequest();
        sendEventRequest.events.add(event);

        when(request.body()).thenReturn(Optional.of(Strings.bytes(JSON.toJSON(sendEventRequest))));

        var controller = new EventController(List.of(), null);
        SendEventRequest parsedSendEventRequest = controller.sendEventRequest(request);

        assertThat(parsedSendEventRequest).usingRecursiveComparison().isEqualTo(sendEventRequest);
    }

    @Test
    void sendEventRequestWithEmptyBody() {
        var controller = new EventController(List.of(), null);

        when(request.body()).thenReturn(Optional.empty());
        when(request.header(HTTPHeaders.CONTENT_TYPE)).thenReturn(Optional.of(ContentType.APPLICATION_JSON.toString()));
        assertThatThrownBy(() -> controller.sendEventRequest(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("body must not be null");

        when(request.header(HTTPHeaders.CONTENT_TYPE)).thenReturn(Optional.of(ContentType.TEXT_PLAIN.toString()));
        assertThat(controller.sendEventRequest(request)).isNull();
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
