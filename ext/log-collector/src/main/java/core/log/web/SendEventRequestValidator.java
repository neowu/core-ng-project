package core.log.web;

import core.framework.internal.validate.Validator;
import core.framework.util.Strings;
import core.framework.web.exception.BadRequestException;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class SendEventRequestValidator {
    private static final int MAX_CONTEXT_VALUE_LENGTH = 1000;
    private static final int MAX_INFO_LENGTH = 900000;  // by default kafka message limit is 1M, leave 100k for rest of message (with compression the room is actually large)
    private final Validator<SendEventRequest> validator = Validator.of(SendEventRequest.class);

    void validate(SendEventRequest request) {
        validator.validate(request, false);
        for (SendEventRequest.Event event : request.events) {
            validate(event);
        }
    }

    private void validate(SendEventRequest.Event event) {
        if (event.result == SendEventRequest.Result.OK && event.action == null)
            throw new BadRequestException("action must not be null if result is OK");
        if ((event.result == SendEventRequest.Result.WARN || event.result == SendEventRequest.Result.ERROR)
            && event.errorCode == null)
            throw new BadRequestException("errorCode must not be null if result is WARN/ERROR");

        validateContext(event.context, MAX_CONTEXT_VALUE_LENGTH);

        validateInfo(event.info, MAX_INFO_LENGTH);
    }

    void validateContext(Map<String, String> context, int maxContextValueLength) {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) throw new BadRequestException("context value must not be null, key=" + key);
            if (value.length() > maxContextValueLength)
                throw new BadRequestException(format("context value is too long, key={}, value={}...(truncated)", key, Strings.truncate(value, 200)), "EVENT_TOO_LARGE");
        }
    }

    void validateInfo(Map<String, String> info, int maxInfoLength) {
        int infoLength = 0;
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            infoLength += key.length();
            String value = entry.getValue();
            if (value == null) throw new BadRequestException("info value must not be null, key=" + key);
            infoLength += value.length();
        }
        if (infoLength > maxInfoLength) {
            throw new BadRequestException("info is too long, length=" + infoLength, "EVENT_TOO_LARGE");
        }
    }
}
