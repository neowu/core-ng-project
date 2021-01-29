package core.log.web;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPHeaders;
import core.framework.inject.Inject;
import core.framework.internal.json.JSONReader;
import core.framework.internal.log.LogManager;
import core.framework.internal.validate.ValidationException;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.EventMessage;
import core.framework.web.CookieSpec;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.ForbiddenException;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class EventController {
    private final JSONReader<SendEventRequest> reader = new JSONReader<>(SendEventRequest.class);
    private final SendEventRequestValidator validator = new SendEventRequestValidator();
    private final List<String> allowedOrigins;
    private final List<String> collectCookies;
    private final boolean allowAllOrigins;

    @Inject
    MessagePublisher<EventMessage> eventMessagePublisher;

    public EventController(List<String> allowedOrigins, List<String> collectCookies) {
        this.allowedOrigins = new ArrayList<>(allowedOrigins.size());
        boolean allowAllOrigins = false;
        for (String origin : allowedOrigins) {
            if ("*".equals(origin)) {
                allowAllOrigins = true;
            } else {
                this.allowedOrigins.add(origin);
            }
        }
        this.allowAllOrigins = allowAllOrigins;
        this.collectCookies = collectCookies;
    }

    public Response options(Request request) {
        String origin = request.header("Origin").orElseThrow(() -> new ForbiddenException("access denied"));
        checkOrigin(origin);

        return Response.empty().status(HTTPStatus.OK)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Methods", "POST, PUT, OPTIONS")
                .header("Access-Control-Allow-Headers", "Accept, Content-Type")
                .header("Access-Control-Allow-Credentials", "true");         // allow send cross-domain cookies, refer to https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Credentials
    }

    public Response post(Request request) {
        String origin = request.header("Origin").orElse(null);
        if (origin != null) checkOrigin(origin);    // allow directly call, e.g. mobile app

        processEvents(request, Instant.now());

        Response response = Response.empty();
        if (origin != null) {
            // only need to response CORS headers for browser/ajax
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Allow-Credentials", "true");
        }
        return response;
    }

    private void processEvents(Request request, Instant now) {
        SendEventRequest eventRequest = sendEventRequest(request);
        if (eventRequest == null) return;

        String app = request.pathParam("app");
        String userAgent = request.header(HTTPHeaders.USER_AGENT).orElse(null);
        String clientIP = request.clientIP();
        List<Cookie> cookies = cookies(request);
        for (SendEventRequest.Event event : eventRequest.events) {
            EventMessage message = message(event, app, now);
            addContext(message.context, userAgent, cookies, clientIP);
            eventMessagePublisher.publish(message.id, message);
        }
    }

    void addContext(Map<String, String> context, String userAgent, List<Cookie> cookies, String clientIP) {
        if (userAgent != null) context.put("user_agent", userAgent);
        context.put("client_ip", clientIP);

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                context.put(cookie.name, cookie.value);
            }
        }
    }

    List<Cookie> cookies(Request request) {
        if (collectCookies == null) return null;

        List<Cookie> cookies = new ArrayList<>(collectCookies.size());
        for (String name : collectCookies) {
            request.cookie(new CookieSpec(name))
                    .ifPresent(value -> cookies.add(new Cookie(name, value)));
        }
        return cookies;
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
        byte[] body = request.body().orElse(null);
        if (body == null) {
            String contentType = request.header(HTTPHeaders.CONTENT_TYPE).orElse(null);
            // different browser/privacy setting/plugin impacts sendBeacon behaviour, here to ignore empty body if not ajax with application/json
            if (contentType == null || contentType.startsWith(ContentType.TEXT_PLAIN.mediaType)) return null;
            throw new BadRequestException("body must not be null", "INVALID_HTTP_REQUEST");
        }
        try {
            SendEventRequest eventRequest = reader.fromJSON(body);
            validator.validate(eventRequest);
            return eventRequest;
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e.errorCode(), e);
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage(), "INVALID_HTTP_REQUEST", e);
        }
    }

    void checkOrigin(String origin) {
        if (allowAllOrigins) return;
        for (String allowedOrigin : allowedOrigins) {
            if (origin.endsWith(allowedOrigin)) return;
        }
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
