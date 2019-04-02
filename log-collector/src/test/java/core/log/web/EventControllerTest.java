package core.log.web;

import core.framework.internal.log.message.EventMessage;
import core.framework.web.Request;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class EventControllerTest {
    private Request request;

    @BeforeEach
    void prepare() {
        request = mock(Request.class);
    }

    @Test
    void checkOriginWithWildcard() {
        var controller = new EventController(Set.of("*"));
        controller.checkOrigin("https://localhost");
    }

    @Test
    void allowedOrigin() {
        var controller = new EventController(Set.of("https://local", "https://localhost"));
        controller.checkOrigin("https://localhost");
    }

    @Test
    void options() {
        when(request.header("Origin")).thenReturn(Optional.empty());
        var controller = new EventController(Set.of("*"));
        assertThatThrownBy(() -> controller.options(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void message() {
        var event = new CollectEventRequest.Event();
        event.id = "1";
        event.date = ZonedDateTime.now().minusHours(1);
        event.result = CollectEventRequest.Result.WARN;
        event.errorCode = "NOT_FOUND";
        event.context.put("path", "/path");
        event.info.put("message", "not found");
        event.elapsedTime = 100L;

        var controller = new EventController(Set.of());
        Instant now = event.date.plusHours(1).toInstant();
        EventMessage message = controller.message(event, "test", now);

        assertThat(message.id).isEqualTo(event.id);
        assertThat(message.timestamp).isEqualTo(now);
        assertThat(message.eventTime).isEqualTo(event.date.toInstant());
        assertThat(message.result).isEqualTo("WARN");
        assertThat(message.errorCode).isEqualTo(event.errorCode);
        assertThat(message.action).isNull();
        assertThat(message.context).isEqualTo(event.context);
        assertThat(message.info).isEqualTo(event.info);
        assertThat(message.elapsed).isEqualTo(event.elapsedTime);
        assertThat(message.app).isEqualTo("test");
    }

    @Test
    void validate() {
        var controller = new EventController(Set.of());

        assertThatThrownBy(() -> controller.validate(request(CollectEventRequest.Result.OK, null, null)))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> controller.validate(request(CollectEventRequest.Result.WARN, null, null)))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> controller.validate(request(CollectEventRequest.Result.ERROR, null, null)))
                .isInstanceOf(BadRequestException.class);

        controller.validate(request(CollectEventRequest.Result.OK, "action", null));
        controller.validate(request(CollectEventRequest.Result.WARN, null, "ERROR_CODE"));
        controller.validate(request(CollectEventRequest.Result.ERROR, null, "ERROR_CODE"));
    }

    private CollectEventRequest request(CollectEventRequest.Result result, String action, String errorCode) {
        CollectEventRequest request = new CollectEventRequest();
        var event = new CollectEventRequest.Event();
        event.id = "1";
        event.result = result;
        event.action = action;
        event.errorCode = errorCode;
        request.events.add(event);
        return request;
    }
}
