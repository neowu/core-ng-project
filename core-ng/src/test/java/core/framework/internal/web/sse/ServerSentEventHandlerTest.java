package core.framework.internal.web.sse;

import core.framework.internal.web.http.RateControl;
import core.framework.util.Strings;
import core.framework.web.Request;
import core.framework.web.rate.LimitRate;
import core.framework.web.sse.Channel;
import core.framework.web.sse.ChannelListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ServerSentEventHandlerTest {
    private ServerSentEventHandler handler;

    @BeforeEach
    void createServerSentEventHandler() {
        handler = new ServerSentEventHandler(null, null, null);
    }

    @Test
    void errorResponse() {
        byte[] error = handler.errorResponse(Strings.bytes("{\"error_code\": \"NOT_FOUND\"}"));
        assertThat(error).asString().isEqualTo("""
            retry: 86400000
            
            event: error
            data: {"error_code": "NOT_FOUND"}
            
            """);
    }

    @Test
    void limitRate() {
        RateControl rateControl = mock(RateControl.class);
        ChannelSupport<Object> support = new ChannelSupport<>(new TestListener(), Object.class, null);

        handler.limitRate(rateControl, support, "192.168.1.1");

        verify(rateControl).validateRate("sse", "192.168.1.1");
    }

    static class TestListener implements ChannelListener<Object> {
        @LimitRate("sse")
        @Override
        public void onConnect(Request request, Channel<Object> channel, String lastEventId) {
        }
    }
}
