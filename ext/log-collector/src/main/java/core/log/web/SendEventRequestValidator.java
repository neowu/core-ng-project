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
    private static final int MAX_KEY_LENGTH = 50;
    private static final int MAX_CONTEXT_VALUE_LENGTH = 1000;
    private static final int MAX_INFO_VALUE_LENGTH = 500_000;
    private static final int MAX_ESTIMATED_LENGTH = 900_000;  // by default kafka message limit is 1M, leave 100k for rest of message (with compression the room is actually larger)
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

        int estimatedLength = 0;
        estimatedLength += validateMap(event.context, MAX_KEY_LENGTH, MAX_CONTEXT_VALUE_LENGTH);
        estimatedLength += validateMap(event.info, MAX_KEY_LENGTH, MAX_INFO_VALUE_LENGTH);
        estimatedLength += validateStats(event.stats, MAX_KEY_LENGTH);
        if (estimatedLength > MAX_ESTIMATED_LENGTH) throw new BadRequestException("event is too large, estimatedLength=" + estimatedLength, "EVENT_TOO_LARGE");
    }

    int validateMap(Map<String, String> map, int maxKeyLength, int maxValueLength) {
        int estimatedLength = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.length() > maxKeyLength)
                throw new BadRequestException(format("key is too long, key={}...(truncated)", Strings.truncate(key, 50)), "EVENT_TOO_LARGE");
            estimatedLength += key.length();

            String value = entry.getValue();
            if (value == null) throw new BadRequestException("value must not be null, key=" + key);
            if (value.length() > maxValueLength)
                throw new BadRequestException(format("value is too long, key={}, value={}...(truncated)", key, Strings.truncate(value, 200)), "EVENT_TOO_LARGE");
            estimatedLength += value.length();
        }
        return estimatedLength;
    }

    int validateStats(Map<String, Double> stats, int maxKeyLength) {
        int estimatedLength = 0;
        for (String key : stats.keySet()) {
            if (key.length() > maxKeyLength)
                throw new BadRequestException(format("key is too long, key={}...(truncated)", Strings.truncate(key, 50)), "EVENT_TOO_LARGE");
            estimatedLength += key.length() + 5;    // estimate double value as 5 chars
        }
        return estimatedLength;
    }
}
