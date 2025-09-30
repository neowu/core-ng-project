package core.framework.internal.async;

import core.framework.internal.stat.Counter;

public class VirtualThread {
    // virtual thread info can be obtained thru jdk.internal.vm.ThreadContainers.root()
    // refer to jdk.internal.vm.ThreadDumper.dumpThreads(java.io.PrintStream)
    // use simple counter before JDK provides virtual thread info API/mbeans
    // ref: jdk.management.VirtualThreadSchedulerMXBean getQueuedVirtualThreadCount() is for virtual threads not yet started
    public static final Counter COUNT = new Counter();
}
