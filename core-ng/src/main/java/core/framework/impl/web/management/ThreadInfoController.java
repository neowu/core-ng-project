package core.framework.impl.web.management;

import core.framework.web.Request;
import core.framework.web.Response;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * @author neo
 */
public class ThreadInfoController {
    public Response threadUsage(Request request) {
        ControllerHelper.assertFromLocalNetwork(request.clientIP());

        ThreadUsage usage = new ThreadUsage();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        usage.threadCount = threadMXBean.getThreadCount();
        usage.peakThreadCount = threadMXBean.getPeakThreadCount();

        return Response.bean(usage);
    }

    public Response threadDump(Request request) {
        ControllerHelper.assertFromLocalNetwork(request.clientIP());

        return Response.text(threadDumpText());
    }

    String threadDumpText() {
        StringBuilder builder = new StringBuilder();
        ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        for (ThreadInfo thread : threads) {
            appendThreadInfo(builder, thread);
        }
        return builder.toString();
    }

    // port from ThreadInfo.toString, to print all stack frames (ThreadInfo.toString() only print 8 frames)
    private void appendThreadInfo(StringBuilder builder, ThreadInfo threadInfo) {
        builder.append('\"').append(threadInfo.getThreadName())
               .append("\" Id=").append(threadInfo.getThreadId())
               .append(' ').append(threadInfo.getThreadState());
        if (threadInfo.getLockName() != null) {
            builder.append(" on ").append(threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            builder.append(" owned by \"").append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            builder.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            builder.append(" (in native)");
        }
        builder.append('\n');
        appendStackTrace(builder, threadInfo);

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            builder.append("\n\tNumber of locked synchronizers = ").append(locks.length);
            builder.append('\n');
            for (LockInfo lock : locks) {
                builder.append("\t- ").append(lock);
                builder.append('\n');
            }
        }
        builder.append('\n');
    }

    private void appendStackTrace(StringBuilder builder, ThreadInfo threadInfo) {
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        int length = stackTrace.length;
        for (int i = 0; i < length; i++) {
            StackTraceElement stack = stackTrace[i];
            builder.append("\tat ").append(stack);
            builder.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State threadState = threadInfo.getThreadState();
                switch (threadState) {
                    case BLOCKED:
                        builder.append("\t-  blocked on ").append(threadInfo.getLockInfo());
                        builder.append('\n');
                        break;
                    case WAITING:
                        builder.append("\t-  waiting on ").append(threadInfo.getLockInfo());
                        builder.append('\n');
                        break;
                    case TIMED_WAITING:
                        builder.append("\t-  timed-waiting on ").append(threadInfo.getLockInfo());
                        builder.append('\n');
                        break;
                    default:
                        break;
                }
            }

            for (MonitorInfo monitorInfo : threadInfo.getLockedMonitors()) {
                if (monitorInfo.getLockedStackDepth() == i) {
                    builder.append("\t-  locked ").append(monitorInfo);
                    builder.append('\n');
                }
            }
        }
    }
}
