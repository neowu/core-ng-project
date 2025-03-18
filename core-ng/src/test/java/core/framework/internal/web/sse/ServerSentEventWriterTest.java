package core.framework.internal.web.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerSentEventWriterTest {
    private ServerSentEventWriter<TestEvent> builder;

    @BeforeEach
    void createServerSentEventBuilder() {
        builder = new ServerSentEventWriter<>(TestEvent.class);
    }

    @Test
    void message() {
        assertThat(builder.message("001", "message"))
            .asString().isEqualTo("id: 001\ndata: message\n\n");

        assertThat(builder.message(null, "message"))
            .asString().isEqualTo("data: message\n\n");
    }
}
