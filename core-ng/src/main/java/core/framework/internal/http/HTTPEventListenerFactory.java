package core.framework.internal.http;

import core.framework.log.ActionLogContext;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

/**
 * @author neo
 */
public class HTTPEventListenerFactory implements EventListener.Factory {
    @Override
    public EventListener create(Call call) {
        return new Listener();
    }

    static class Listener extends EventListener {   // okHTTP create listener for each realCall, no threading issue
        private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
        private long dnsStart;
        private long connectStart;

        // okHTTP uses URL Host/Address as key to acquire connection, refer to okhttp3.internal.connection.RealConnectionPool -> callAcquirePooledConnection
        // if conn pool doesn't have matched connection, it creates new connection object, resolves dns(dnsStart/End), connects socket (connectStart/End)
        // if conn pool has eligible connection, then reuses connection, dns/connect are skipped
        @Override
        public void dnsStart(Call call, String domain) {
            dnsStart = System.nanoTime();
        }

        @Override
        public void dnsEnd(Call call, String domain, List<InetAddress> addresses) {
            long elapsed = System.nanoTime() - dnsStart;
            LOGGER.debug("resolve domain, domain={}, addresses={}, elapsed={}", domain, addresses, elapsed);
            ActionLogContext.track("http_dns", elapsed);
        }

        @Override
        public void connectStart(Call call, InetSocketAddress address, Proxy proxy) {
            connectStart = System.nanoTime();
        }

        @Override
        public void connectEnd(Call call, InetSocketAddress address, Proxy proxy, Protocol protocol) {
            long elapsed = System.nanoTime() - connectStart;
            LOGGER.debug("create http connection, address={}, elapsed={}", address, elapsed);
            ActionLogContext.track("http_conn", elapsed);
        }
    }
}
