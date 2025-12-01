package core.framework.internal.web.service;

import core.framework.api.json.Property;
import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public class InternalErrorResponse {    // keep compatible with ErrorResponse with additional severity/stackTrace fields
    @Nullable
    @Property(name = "id")
    public String id;

    @Property(name = "severity")
    public String severity;

    @Nullable
    @Property(name = "errorCode")
    public String errorCode;

    @Property(name = "message")
    public String message;

    @Property(name = "stackTrace")
    public String stackTrace;
}
