package core.log.web;

import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class SendEventRequestValidatorTest {
    private SendEventRequestValidator validator;

    @BeforeEach
    void createCollectEventRequestValidator() {
        validator = new SendEventRequestValidator();
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> validator.validate(request(SendEventRequest.Result.OK, null, null)))
            .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> validator.validate(request(SendEventRequest.Result.WARN, null, null)))
            .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> validator.validate(request(SendEventRequest.Result.ERROR, null, null)))
            .isInstanceOf(BadRequestException.class);

        validator.validate(request(SendEventRequest.Result.OK, "action", null));
        validator.validate(request(SendEventRequest.Result.WARN, null, "ERROR_CODE"));
        validator.validate(request(SendEventRequest.Result.ERROR, null, "ERROR_CODE"));
    }

    @Test
    void validateMap() {
        assertThatThrownBy(() -> validator.validateMap(Map.of("too_long_key", "value"), 5, 50))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("key is too long");

        assertThatThrownBy(() -> validator.validateMap(Map.of("context", "12345"), 10, 3))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("value is too long");

        Map<String, String> context = new HashMap<>();
        context.put("session_id", null);
        assertThatThrownBy(() -> validator.validateMap(context, 10, 10))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("value must not be null, key=session_id");

        int estimatedLength = validator.validateMap(Map.of("key", "value"), 20, 20);
        assertThat(estimatedLength).isEqualTo(8);
    }

    @Test
    void validateStats() {
        assertThatThrownBy(() -> validator.validateStats(Map.of("too_long_key", 1.0), 5))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("key is too long");

        int estimatedLength = validator.validateStats(Map.of("key", 1.0), 20);
        assertThat(estimatedLength).isEqualTo(8);
    }

    private SendEventRequest request(SendEventRequest.Result result, String action, String errorCode) {
        SendEventRequest request = new SendEventRequest();
        var event = new SendEventRequest.Event();
        event.date = ZonedDateTime.now();
        event.result = result;
        event.action = action;
        event.errorCode = errorCode;
        event.elapsedTime = 1000L;
        request.events.add(event);
        return request;
    }
}
