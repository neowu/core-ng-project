package core.framework.impl.web.site;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class AJAXErrorResponse {
    @Property(name = "id")
    public String id;

    @Property(name = "errorCode")   // for ajax, camel case is more js friendly
    public String errorCode;

    @Property(name = "message")
    public String message;
}
