package core.log.web;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPHeaders;
import core.framework.inject.Inject;
import core.framework.internal.log.message.EventMessage;
import core.framework.kafka.MessagePublisher;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.ForbiddenException;

import java.time.Instant;
import java.util.Set;

/**
 * @author neo
 */
public class EventController {
    private final Set<String> allowedOrigins;
    @Inject
    MessagePublisher<EventMessage> eventMessagePublisher;

    public EventController(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public Response options(Request request) {
        String allowedOrigin = allowedOrigin(request);

        return Response.empty().status(HTTPStatus.OK)
                       .header("Access-Control-Allow-Origin", allowedOrigin)
                       .header("Access-Control-Allow-Methods", "PUT, OPTIONS")
                       .header("Access-Control-Allow-Headers", "Accept, Content-Type");
    }

    String allowedOrigin(Request request) {
        String origin = request.header("Origin").orElseThrow(() -> new ForbiddenException("access denied"));
        if (allowedOrigins.contains("*")) return "*";
        if (allowedOrigins.contains(origin)) return origin;
        throw new ForbiddenException("access denied");
    }

    public Response put(Request request) {
        String allowedOrigin = allowedOrigin(request);

        String app = request.pathParam("app");
        String userAgent = request.header(HTTPHeaders.USER_AGENT).orElse(null);
        Instant now = Instant.now();
        String clientIP = request.clientIP();

        CollectEventRequest eventRequest = request.bean(CollectEventRequest.class);
        for (CollectEventRequest.Event event : eventRequest.events) {
            EventMessage message = message(event);
            message.app = app;
            message.timestamp = now;
            if (userAgent != null) message.context.put("userAgent", userAgent);
            message.context.put("clientIP", clientIP);

            eventMessagePublisher.publish(message.id, message);
        }

        return Response.empty()
                       .header("Access-Control-Allow-Origin", allowedOrigin);
    }

    private EventMessage message(CollectEventRequest.Event event) {
        var message = new EventMessage();
        message.id = event.id;
        message.eventTime = event.date.toInstant();
        message.type = event.type;
        message.result = String.valueOf(event.result);
        message.context = event.context;
        message.errorMessage = event.errorMessage;
        message.exceptionStackTrace = event.exceptionStackTrace;
        message.elapsed = event.elapsedTime;
        return message;
    }
}
