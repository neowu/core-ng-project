package core.framework.internal.web.sse;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelSupportTest {
    private ChannelSupport<TestEvent> builder;

    @BeforeEach
    void createChannelSupport() {
        builder = new ChannelSupport<>(null, TestEvent.class, null);
    }

    @Test
    void message() {
        assertThat(builder.message("001", Strings.bytes("message")))
            .asString().isEqualTo("id:001\ndata:message\n\n");

        assertThat(builder.message(null, Strings.bytes("message")))
            .asString().isEqualTo("data:message\n\n");
    }

}
