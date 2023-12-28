package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.stat.Diagnostic;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.util.Files;
import core.framework.web.Request;
import core.framework.web.Response;

import java.nio.file.Path;

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

    public Response virtualThread(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(Diagnostic.virtualThread());
    }

    public Response heap(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(Diagnostic.heap());
    }

    public Response proc(Request request) {
        accessControl.validate(request.clientIP());

        Path procPath = Path.of("/proc/self/status");
        if (java.nio.file.Files.exists(procPath)) {
            return Response.bytes(Files.bytes(procPath))
                    .contentType(ContentType.TEXT_PLAIN);
        } else {
            return Response.text("/proc/self/status not found");
        }
    }
}
