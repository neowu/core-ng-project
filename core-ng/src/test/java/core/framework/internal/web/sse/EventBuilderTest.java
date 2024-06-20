package core.framework.internal.web.sse;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventBuilderTest {
    private EventBuilder<TestEvent> builder;

    @BeforeEach
    void createEventBuilder() {
        builder = new EventBuilder<>(TestEvent.class);
    }

    @Test
    void message() {
        assertThat(builder.message("001", Strings.bytes("message")))
            .asString().isEqualTo("id:001\ndata:message\n\n");

        assertThat(builder.message(null, Strings.bytes("message")))
            .asString().isEqualTo("data:message\n\n");
    }

}
