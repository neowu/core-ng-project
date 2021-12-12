package core.framework.internal.http;

import core.framework.internal.log.LogManager;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Protocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class HTTPEventListenerFactoryTest {
    private HTTPEventListenerFactory factory;

    @BeforeEach
    void createHTTPEventListenerFactory() {
        factory = new HTTPEventListenerFactory();
    }

    @Test
    void create() {
        Call call = mock(Call.class);
        Proxy proxy = mock(Proxy.class);
        var logManager = new LogManager();

        logManager.begin("begin", null);
        EventListener listener = factory.create(call);
        listener.dnsStart(call, "domain");
        listener.dnsEnd(call, "domain", List.of());
        listener.connectStart(call, new InetSocketAddress(8443), proxy);
        listener.connectEnd(call, new InetSocketAddress(8443), proxy, Protocol.HTTP_2);
        listener.responseHeadersStart(call);
        listener.responseBodyEnd(call, -1);
        logManager.end("end");
    }
}
