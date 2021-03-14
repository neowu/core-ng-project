package app.monitor.channel;

import app.monitor.alert.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ChannelManagerTest {
    private ChannelManager channelManager;
    @Mock
    private Channel slackChannel;
    @Mock
    private Channel pagerChannel;

    @BeforeEach
    void createChannelManager() {
        channelManager = new ChannelManager(Map.of("slack", slackChannel, "pager", pagerChannel), "slack");
    }

    @Test
    void parseChannelURI() {
        assertThat(channelManager.parseChannelURI("slack://channelId")).isEqualTo(new String[]{"slack", "channelId"});
        assertThat(channelManager.parseChannelURI("channelId")).isEqualTo(new String[]{null, "channelId"});
    }

    @Test
    void notifyWithPagerChannel() {
        var alert = new Alert();
        channelManager.notify("pager://channelId", alert, 1);

        verify(pagerChannel).notify("channelId", alert, 1);
    }

    @Test
    void notifyWithDefaultChannel() {
        var alert = new Alert();
        channelManager.notify("channelId", alert, 1);

        verify(slackChannel).notify("channelId", alert, 1);
    }
}
