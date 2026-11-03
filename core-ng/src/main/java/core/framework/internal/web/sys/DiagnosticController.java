package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.stat.Diagnostic;
import core.framework.internal.web.http.IPAccessControl;
import core.framework.util.Files;
import core.framework.web.Request;
import core.framework.web.Response;

import java.nio.file.Path;

/**
 * @author neo
 */
public class DiagnosticController {
    // add -XX:NativeMemoryTracking=summary or -XX:NativeMemoryTracking=detail to enable native memory tracking, and vmInfo will include NMT summary
    // enabling NMT will result in a 5-10 percent JVM performance drop
    public Response vm(Request request) {
        IPAccessControl.validateLocalIP(request.remoteIP());
        return Response.text(Diagnostic.vm());
    }

    public Response thread(Request request) {
        IPAccessControl.validateLocalIP(request.remoteIP());
        return Response.text(Diagnostic.thread());
    }

    public Response virtualThread(Request request) {
        IPAccessControl.validateLocalIP(request.remoteIP());
        return Response.text(Diagnostic.virtualThread());
    }

    public Response heap(Request request) {
        IPAccessControl.validateLocalIP(request.remoteIP());
        return Response.text(Diagnostic.heap());
    }

    public Response proc(Request request) {
        IPAccessControl.validateLocalIP(request.remoteIP());

        Path procPath = Path.of("/proc/self/status");
        if (java.nio.file.Files.exists(procPath)) {
            return Response.bytes(Files.bytes(procPath))
                    .contentType(ContentType.TEXT_PLAIN);
        } else {
            return Response.text("/proc/self/status not found");
        }
    }
}
