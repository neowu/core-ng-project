package core.framework.impl.web.service;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class ErrorResponse {
    @Property(name = "id")
    public String id;

    @Property(name = "severity")
    public String severity;

    @Property(name = "error_code")
    public String errorCode;

    @Property(name = "message")
    public String message;

    @Property(name = "stack_trace")
    public String stackTrace;
}
