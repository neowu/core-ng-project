package core.framework.internal.http;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
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
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return EventListener.NONE;
        return new Listener(actionLog);
    }

    static class Listener extends EventListener {   // okHTTP create listener for each realCall, no threading issue
        private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
        private final ActionLog actionLog;
        private long dnsStart;
        private long connectStart;
        private long readStart;

        Listener(ActionLog actionLog) {
            this.actionLog = actionLog;
        }

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
            actionLog.track("http_dns", elapsed, 0, 0);
        }

        @Override
        public void connectStart(Call call, InetSocketAddress address, Proxy proxy) {
            connectStart = System.nanoTime();
        }

        @Override
        public void connectEnd(Call call, InetSocketAddress address, Proxy proxy, Protocol protocol) {
            long elapsed = System.nanoTime() - connectStart;
            LOGGER.debug("create http connection, address={}, elapsed={}", address, elapsed);
            actionLog.track("http_conn", elapsed, 0, 0);
        }

        @Override
        public void responseHeadersStart(Call call) {
            readStart = System.nanoTime();
        }

        @Override
        public void responseBodyEnd(Call call, long byteCount) {
            long elapsed = System.nanoTime() - readStart;
            actionLog.track("http_read", elapsed, 0, 0);
        }
    }
}
