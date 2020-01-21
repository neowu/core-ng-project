package app.monitor.slack;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

/**
 * @author neo
 */
class SlackClientTest {
    private SlackClient client;

    @BeforeEach
    void createSlackClient() {
        client = new SlackClient(null);
    }

    @Test
    void request() {
        SlackMessageAPIRequest request = client.request("channel", "message", "color");
        assertThat(request.attachments).hasSize(1)
                                       .has(new Condition<>(attachment -> "message".equals(attachment.text), "attachment.text should be message"),
                                               atIndex(0));
    }
}
