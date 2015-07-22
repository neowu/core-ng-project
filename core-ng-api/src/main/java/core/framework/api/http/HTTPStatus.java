package core.framework.api.http;

/**
 * @author neo
 */
public enum HTTPStatus {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    MOVED_PERMANENTLY(301),
    SEE_OTHER(303),
    NOT_MODIFIED(304),
    TEMPORARY_REDIRECT(307),
    PERMANENT_REDIRECT(308),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    INTERNAL_SERVER_ERROR(500),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504);

    public final int code;

    HTTPStatus(int code) {
        this.code = code;
    }

    public static HTTPStatus parse(int code) {
        for (HTTPStatus status : HTTPStatus.values()) {
            if (status.code == code) return status;
        }
        throw new Error("unsupported http status code, code=" + code);
    }
}
