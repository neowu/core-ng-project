package core.framework.internal.module;

import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class ReadinessProbe {
    static final long MAX_WAIT_TIME_IN_NANO = 27_000_000_000L;  // 27s, roughly attempts 6 times if no read timeout
    private final Logger logger = LoggerFactory.getLogger(ReadinessProbe.class);

    // hostURI is in host[:port] format
    public List<String> hostURIs = new ArrayList<>();
    public List<String> urls = new ArrayList<>();

    public void check() throws Exception {
        logger.info("check readiness");
        var watch = new StopWatch();
        checkDNS(watch);
        checkHTTP(watch);
    }

    private void checkDNS(StopWatch watch) throws InterruptedException {
        for (String hostURI : hostURIs) {
            String hostname = hostname(hostURI);
            resolveHost(hostname, watch);
        }
        hostURIs = null;    // release memory
    }

    private void checkHTTP(StopWatch watch) throws InterruptedException {
        if (!urls.isEmpty()) {
            HTTPClient client = HTTPClient.builder().build();
            for (String url : urls) {
                sendHTTPRequest(url, client, watch);
            }
        }
        urls = null;    // release memory
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl") // intentional, simplest way to unify control flow
    private void sendHTTPRequest(String url, HTTPClient client, StopWatch watch) throws InterruptedException {
        var request = new HTTPRequest(HTTPMethod.GET, url);
        while (true) {
            try {
                HTTPResponse response = client.execute(request);
                if (response.statusCode >= 200 && response.statusCode < 300) return;
                throw new Exception("http request failed, status=" + response.statusCode);
            } catch (Exception e) {
                if (watch.elapsed() >= MAX_WAIT_TIME_IN_NANO) throw new Error("readiness check failed, url=" + url, e);
                logger.warn(errorCode("NOT_READY"), "http probe failed, retry soon, url={}", url, e);
                Thread.sleep(5000);
            }
        }
    }

    void resolveHost(String hostname, StopWatch watch) throws InterruptedException {
        while (true) {
            try {
                InetAddress.getByName(hostname);
                return;
            } catch (UnknownHostException e) {
                if (watch.elapsed() >= MAX_WAIT_TIME_IN_NANO) throw new Error("readiness check failed, host=" + hostname, e);
                logger.warn(errorCode("NOT_READY"), "dns probe failed, retry soon, host={}", hostname, e);
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
