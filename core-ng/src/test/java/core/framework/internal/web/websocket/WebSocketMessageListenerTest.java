package core.framework.internal.web.websocket;

import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class WebSocketMessageListenerTest {
    private WebSocketMessageListener listener;

    @BeforeEach
    void createWebSocketMessageListener() {
        listener = new WebSocketMessageListener(null, null);
    }

    @Test
    void closeCode() {
        assertThat(listener.closeCode(new Error()))
                .isEqualTo(WebSocketCloseCodes.INTERNAL_ERROR);

        assertThat(listener.closeCode(new TooManyRequestsException("rate exceeds")))
                .isEqualTo(WebSocketCloseCodes.TRY_AGAIN_LATER);

        assertThat(listener.closeCode(new BadRequestException("bad request")))
                .isEqualTo(WebSocketCloseCodes.POLICY_VIOLATION);
    }

    @Test
    void maxBufferSize() {
        assertThat(listener.getMaxCloseBufferSize()).isGreaterThan(-1);
        assertThat(listener.getMaxPingBufferSize()).isGreaterThan(-1);
        assertThat(listener.getMaxPongBufferSize()).isGreaterThan(-1);
        assertThat(listener.getMaxTextBufferSize()).isGreaterThan(-1);
    }
}
