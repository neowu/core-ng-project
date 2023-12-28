package core.framework.internal.stat;

import core.framework.util.Files;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;

/**
 * @author neo
 */
public class Diagnostic {
    public static String thread() {
        return invokeDiagnosticCommand("threadPrint", "-e", "-l");
    }

    public static String vm() {
        return invokeDiagnosticCommand("vmInfo");
    }

    public static String heap() {
        return invokeDiagnosticCommand("gcClassHistogram");
    }

    // enable by -XX:NativeMemoryTracking=summary
    public static String nativeMemory() {
        return invokeDiagnosticCommand("vmNativeMemory", "summary");
    }

    // currently Java 21 only support dump virtual thread to file
    // refer to com.sun.management.internal.HotSpotDiagnostic.dumpThreads(java.lang.String, com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat)
    public static String virtualThread() {
        Path path = Files.tempFile();
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            var name = new ObjectName("com.sun.management", "type", "HotSpotDiagnostic");
            server.invoke(name, "dumpThreads", new Object[]{path.toAbsolutePath().toString(), "TEXT_PLAIN"}, new String[]{String.class.getName(), String.class.getName()});
            return Files.text(path);
        } catch (MalformedObjectNameException | InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new Error(e);
        } finally {
            Files.delete(path);
        }
    }

    // use "jcmd pid help" to list all operations,
    // refer to com.sun.management.internal.DiagnosticCommandImpl.getMBeanInfo, all command names are transformed
    private static String invokeDiagnosticCommand(String operation, String... params) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            var name = new ObjectName("com.sun.management", "type", "DiagnosticCommand");
            return (String) server.invoke(name, operation, new Object[]{params}, new String[]{String[].class.getName()});
        } catch (MalformedObjectNameException | InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new Error(e);
        }
    }
}
