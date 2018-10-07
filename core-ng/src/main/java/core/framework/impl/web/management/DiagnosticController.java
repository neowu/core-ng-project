package core.framework.impl.web.management;

import core.framework.impl.web.http.IPAccessControl;
import core.framework.web.Request;
import core.framework.web.Response;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;

/**
 * @author neo
 */
public class DiagnosticController {
    private final IPAccessControl accessControl = new IPAccessControl();

    public Response vm(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(invoke("vmInfo"));
    }

    public Response thread(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(invoke("threadPrint"));
    }

    public Response heap(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(invoke("gcClassHistogram"));
    }

    private String invoke(String operation) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            var name = new ObjectName("com.sun.management", "type", "DiagnosticCommand");
            return (String) server.invoke(name, operation, new Object[]{null}, new String[]{String[].class.getName()});
        } catch (MalformedObjectNameException | InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new Error(e);
        }
    }
}
