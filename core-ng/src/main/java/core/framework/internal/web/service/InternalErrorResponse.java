package core.framework.internal.web.service;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class InternalErrorResponse {    // keep compatible with ErrorResponse with additional severity/stackTrace fields
    @Property(name = "id")
    public String id;

    @Property(name = "severity")
    public String severity;

    @Property(name = "errorCode")
    public String errorCode;

    @Property(name = "message")
    public String message;

    @Property(name = "stackTrace")
    public String stackTrace;
}
