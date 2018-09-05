package core.framework.impl.web.websocket;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ChannelImplTest {
    @Test
    void asKey() {
        var channel = new ChannelImpl(null, null, null);
        assertThat(channel).isEqualTo(channel);
        assertThat(channel).hasSameHashCodeAs(channel);
    }
}
