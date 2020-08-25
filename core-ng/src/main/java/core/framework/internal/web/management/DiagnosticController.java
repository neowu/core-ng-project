package core.framework.internal.web.management;

import core.framework.internal.stat.Diagnostic;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.web.Request;
import core.framework.web.Response;

/**
 * @author neo
 */
public class DiagnosticController {
    private final IPv4AccessControl accessControl = new IPv4AccessControl();

    // add -XX:NativeMemoryTracking=summary or -XX:NativeMemoryTracking=detail to enable native memory tracking, and vmInfo will include NMT summary
    // enabling NMT will result in a 5-10 percent JVM performance drop
    public Response vm(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(Diagnostic.vm());
    }

    public Response thread(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(Diagnostic.thread());
    }

    public Response heap(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(Diagnostic.heap());
    }
}
