package core.framework.internal.web.websocket;

import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ChannelHandlerTest {
    private ChannelHandler<TestWebSocketMessage, TestWebSocketMessage> handler;

    @BeforeEach
    void createChannelHandler() {
        handler = new ChannelHandler<>(TestWebSocketMessage.class, TestWebSocketMessage.class, new TestChannelListener());
    }

    @Test
    void toServerMessage() {
        var message = new TestWebSocketMessage();
        message.message = "value";
        assertThat(handler.toServerMessage(message)).isEqualTo("{\"message\":\"value\"}");

        assertThatThrownBy(() -> handler.toServerMessage(new MismatchTestMessage()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("message class does not match");
    }

    @Test
    void fromClientMessage() {
        assertThatThrownBy(() -> handler.fromClientMessage("message"))
                .isInstanceOf(BadRequestException.class)
                .satisfies(e -> assertThat(((BadRequestException) e).errorCode()).isEqualTo("INVALID_WS_MESSAGE"));

        assertThatThrownBy(() -> handler.fromClientMessage("{}"))
                .isInstanceOf(BadRequestException.class)
                .satisfies(e -> assertThat(((BadRequestException) e).errorCode()).isEqualTo("VALIDATION_ERROR"));
    }

    static class MismatchTestMessage extends TestWebSocketMessage {
    }
}
