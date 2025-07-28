package core.framework.internal.web.service;

import core.framework.api.json.Property;
import core.framework.log.ErrorCode;

/**
 * @author neo
 */
public final class ErrorResponse {
    public static ErrorResponse errorResponse(Throwable e, String actionId) {
        var response = new ErrorResponse();
        response.id = actionId;
        response.message = e.getMessage();
        if (e instanceof ErrorCode code) {
            response.errorCode = code.errorCode();
        } else {
            response.errorCode = "INTERNAL_ERROR";
        }
        return response;
    }

    @Property(name = "id")
    public String id;
    @Property(name = "errorCode")
    public String errorCode;
    @Property(name = "message")
    public String message;
}
