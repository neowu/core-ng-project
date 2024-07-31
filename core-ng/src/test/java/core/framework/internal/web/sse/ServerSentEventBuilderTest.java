package core.framework.internal.web.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerSentEventBuilderTest {
    private ServerSentEventBuilder<TestEvent> builder;

    @BeforeEach
    void createServerSentEventBuilder() {
        builder = new ServerSentEventBuilder<>(TestEvent.class);
    }

    @Test
    void build() {
        assertThat(builder.build("001", "message"))
            .asString().isEqualTo("id: 001\ndata: message\n\n");

        assertThat(builder.build(null, "message"))
            .asString().isEqualTo("data: message\n\n");
    }
}
