package core.framework.internal.web.websocket;

import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ChannelSupportTest {
    private ChannelSupport<TestWebSocketMessage, TestWebSocketMessage> holder;

    @BeforeEach
    void createChannelHandler() {
        holder = new ChannelSupport<>(TestWebSocketMessage.class, TestWebSocketMessage.class, new TestChannelListener(), null);
    }

    @Test
    void toServerMessage() {
        var message = new TestWebSocketMessage();
        message.message = "value";
        assertThat(holder.toServerMessage(message)).isEqualTo("{\"message\":\"value\"}");
    }

    @Test
    void fromClientMessage() {
        assertThatThrownBy(() -> holder.fromClientMessage("message"))
            .isInstanceOf(BadRequestException.class)
            .satisfies(e -> assertThat(((BadRequestException) e).errorCode()).isEqualTo("INVALID_WS_MESSAGE"));

        assertThatThrownBy(() -> holder.fromClientMessage("{}"))
            .isInstanceOf(BadRequestException.class)
            .satisfies(e -> assertThat(((BadRequestException) e).errorCode()).isEqualTo("VALIDATION_ERROR"));
    }
}
