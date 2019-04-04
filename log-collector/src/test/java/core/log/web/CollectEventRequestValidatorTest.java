package core.log.web;

import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class CollectEventRequestValidatorTest {
    private CollectEventRequestValidator validator;

    @BeforeEach
    void createCollectEventRequestValidator() {
        validator = new CollectEventRequestValidator();
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> validator.validate(request(CollectEventRequest.Result.OK, null, null)))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> validator.validate(request(CollectEventRequest.Result.WARN, null, null)))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> validator.validate(request(CollectEventRequest.Result.ERROR, null, null)))
                .isInstanceOf(BadRequestException.class);

        validator.validate(request(CollectEventRequest.Result.OK, "action", null));
        validator.validate(request(CollectEventRequest.Result.WARN, null, "ERROR_CODE"));
        validator.validate(request(CollectEventRequest.Result.ERROR, null, "ERROR_CODE"));
    }

    @Test
    void validateWithTooLongContextValue() {
        CollectEventRequest request = request(CollectEventRequest.Result.OK, "action", null);
        request.events.get(0).context.put("context", "x".repeat(1001));
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("too long");
    }

    private CollectEventRequest request(CollectEventRequest.Result result, String action, String errorCode) {
        CollectEventRequest request = new CollectEventRequest();
        var event = new CollectEventRequest.Event();
        event.result = result;
        event.action = action;
        event.errorCode = errorCode;
        request.events.add(event);
        return request;
    }
}
