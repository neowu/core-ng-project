package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.sse.TestChannelListener;
import core.framework.internal.web.sse.TestEvent;
import core.framework.util.Types;
import core.framework.web.sse.ServerSentEventContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerSentEventConfigTest {
    private ServerSentEventConfig config;

    @BeforeAll
    void createWebSocketConfig() {
        config = new ServerSentEventConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void withReservedPath() {
        assertThatThrownBy(() -> config.listen(HTTPMethod.GET, HTTPIOHandler.HEALTH_CHECK_PATH, TestEvent.class, new TestChannelListener()))
            .isInstanceOf(Error.class)
            .hasMessageContaining("/health-check is reserved path");
    }

    @Test
    void listen() {
        assertThatThrownBy(() -> config.listen(HTTPMethod.GET, "/sse/:name", TestEvent.class, new TestChannelListener()))
            .isInstanceOf(Error.class)
            .hasMessageContaining("listener path must be static");

        assertThatThrownBy(() -> config.listen(HTTPMethod.GET, "/sse", TestEvent.class, (request, channel, lastEventId) -> {
        })).isInstanceOf(Error.class)
            .hasMessageContaining("listener class must not be anonymous class or lambda");

        config.listen(HTTPMethod.GET, "/sse2", TestEvent.class, new TestChannelListener());
        @SuppressWarnings("unchecked")
        ServerSentEventContext<TestEvent> context = (ServerSentEventContext<TestEvent>) this.config.context.beanFactory.bean(Types.generic(ServerSentEventContext.class, TestEvent.class), null);
        assertThat(context).isNotNull();
        assertThat(this.config.context.apiController.beanClasses).contains(TestEvent.class);
    }
}
