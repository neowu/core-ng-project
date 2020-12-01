package core.framework.internal.stat;

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
public class Diagnostic {
    public static String thread() {
        return invoke("threadPrint", "-e");
    }

    public static String vm() {
        return invoke("vmInfo");
    }

    public static String heap() {
        return invoke("gcClassHistogram");
    }

    // enable by -XX:NativeMemoryTracking=summary
    public static String nativeMemory() {
        return invoke("vmNativeMemory", "summary");
    }

    // use "jcmd pid help" to list all operations,
    // refer to com.sun.management.internal.DiagnosticCommandImpl.getMBeanInfo, all command names are transformed
    private static String invoke(String operation, String... params) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            var name = new ObjectName("com.sun.management", "type", "DiagnosticCommand");
            return (String) server.invoke(name, operation, new Object[]{params}, new String[]{String[].class.getName()});
        } catch (MalformedObjectNameException | InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new Error(e);
        }
    }
}
