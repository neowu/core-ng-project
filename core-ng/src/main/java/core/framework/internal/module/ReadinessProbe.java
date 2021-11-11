package core.framework.internal.module;

import core.framework.async.Task;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class ReadinessProbe implements Task {
    // hostURI are in host:[port] format
    public final List<String> hostURIs = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(ReadinessProbe.class);

    @Override
    public void execute() throws Exception {
        var watch = new StopWatch();
        for (String hostURI : hostURIs) {
            String hostname = hostname(hostURI);
            resolveHost(hostname, watch);
        }
    }

    void resolveHost(String hostname, StopWatch watch) throws UnknownHostException, InterruptedException {
        while (true) {
            try {
                InetAddress.getByName(hostname);
                return;
            } catch (UnknownHostException e) {
                if (watch.elapsed() >= 30_000_000_000L) throw e;
                logger.warn("failed to resolve host, retry soon, host={}", hostname, e);
                Thread.sleep(5000);
            }
        }
    }

    String hostname(String hostURI) {
        int index = hostURI.indexOf(':');
        if (index == -1) return hostURI;
        return hostURI.substring(0, index);
    }
}
