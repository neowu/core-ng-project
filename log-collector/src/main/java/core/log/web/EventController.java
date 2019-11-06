package core.log.web;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPHeaders;
import core.framework.inject.Inject;
import core.framework.internal.log.LogManager;
import core.framework.json.Bean;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.EventMessage;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.ForbiddenException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

/**
 * @author neo
 */
public class EventController {
    private final Set<String> allowedOrigins;
    @Inject
    MessagePublisher<EventMessage> eventMessagePublisher;
    @Inject
    SendEventRequestValidator validator;

    public EventController(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public Response options(Request request) {
        String origin = request.header("Origin").orElseThrow(() -> new ForbiddenException("access denied"));
        checkOrigin(origin);

        return Response.empty().status(HTTPStatus.OK)
                       .header("Access-Control-Allow-Origin", origin)
                       .header("Access-Control-Allow-Methods", "POST, PUT, OPTIONS")
                       .header("Access-Control-Allow-Headers", "Accept, Content-Type");
    }

    public Response post(Request request) {
        String origin = request.header("Origin").orElse(null);
        if (origin != null)
            checkOrigin(origin);    // allow directly call, e.g. mobile app

        String app = request.pathParam("app");
        String userAgent = request.header(HTTPHeaders.USER_AGENT).orElse(null);
        Instant now = Instant.now();
        String clientIP = request.clientIP();

        SendEventRequest eventRequest = sendEventRequest(request);

        for (SendEventRequest.Event event : eventRequest.events) {
            EventMessage message = message(event, app, now);

            if (userAgent != null) message.context.put("userAgent", userAgent);
            message.context.put("clientIP", clientIP);

            eventMessagePublisher.publish(message.id, message);
        }

        Response response = Response.empty();
        if (origin != null) response.header("Access-Control-Allow-Origin", origin);
        return response;
    }

    // ignore content-type and assume it's json, due to client side may uses "navigator.sendBeacon(url, json);" to send event
    // and navigator.sendBeacon() doesn't preflight for CORS, which triggers following exception
    //
    // VM35:1 Uncaught DOMException: Failed to execute 'sendBeacon' on 'Navigator': sendBeacon() with a Blob whose
    // type is not any of the CORS-safelisted values for the Content-Type request header is disabled temporarily.
    // See http://crbug.com/490015 for details.
    //
    // only work around is to allow client side sends simple request to bypass CORS check, which requires content-type=text/plain
    // refer to https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
    SendEventRequest sendEventRequest(Request request) {
        byte[] body = request.body().orElseThrow(() -> new BadRequestException("body must not be null", "INVALID_HTTP_REQUEST"));
        SendEventRequest eventRequest = Bean.fromJSON(SendEventRequest.class, new String(body, StandardCharsets.UTF_8));
        validator.validate(eventRequest);
        return eventRequest;
    }

    void checkOrigin(String origin) {
        if (allowedOrigins.contains("*") || allowedOrigins.contains(origin)) return;
        throw new ForbiddenException("access denied");
    }

    EventMessage message(SendEventRequest.Event event, String app, Instant now) {
        var message = new EventMessage();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = event.date.toInstant();
        message.app = app;
        message.receivedTime = now;
        message.result = JSON.toEnumValue(event.result);
        message.action = event.action;
        message.errorCode = event.errorCode;
        message.errorMessage = event.errorMessage;
        message.context = event.context;
        message.info = event.info;
        message.stats = event.stats;
        message.elapsed = event.elapsedTime;
        return message;
    }
}
