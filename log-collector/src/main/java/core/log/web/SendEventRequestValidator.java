package core.log.web;

import core.framework.web.exception.BadRequestException;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class SendEventRequestValidator {
    private static final int MAX_CONTEXT_VALUE_LENGTH = 1000;
    private static final int MAX_INFO_LENGTH = 800000;  // kafka message limit is 950k, leave 150k for rest of message

    void validate(SendEventRequest request) {
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

        event.context.forEach((key, value) -> {
            if (value.length() > MAX_CONTEXT_VALUE_LENGTH)
                throw new BadRequestException(format("context value is too long, key={}, value={}...(truncated)", key, value.substring(0, 200)), "EVENT_TOO_LARGE");
        });

        validateInfo(event.info, MAX_INFO_LENGTH);
    }

    void validateInfo(Map<String, String> info, int maxInfoLength) {
        int infoLength = 0;
        for (Map.Entry<String, String> entry : info.entrySet()) {
            infoLength += entry.getKey().length();
            infoLength += entry.getValue().length();
        }
        if (infoLength > maxInfoLength) {
            throw new BadRequestException(format("info is too long, length={}", infoLength), "EVENT_TOO_LARGE");
        }
    }
}
