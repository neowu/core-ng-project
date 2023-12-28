package app.monitor.channel;

import app.monitor.alert.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ChannelManagerTest {
    @Mock
    Channel slackChannel;
    @Mock
    Channel pagerChannel;
    private ChannelManager channelManager;

    @BeforeEach
    void createChannelManager() {
        channelManager = new ChannelManager(Map.of("slack", slackChannel, "pagerduty", pagerChannel), "slack");
    }

    @Test
    void parseChannelURI() {
        ChannelURI uri = channelManager.parseChannelURI("slack://channelId");
        assertThat(uri.type).isEqualTo("slack");
        assertThat(uri.id).isEqualTo("channelId");
        assertThat(uri.params).isNotNull();

        uri = channelManager.parseChannelURI("channelId");
        assertThat(uri.type).isNull();
        assertThat(uri.id).isEqualTo("channelId");
        assertThat(uri.params).isNotNull();

        uri = channelManager.parseChannelURI("pagerduty://serviceId?priorityId=mockPriorityId&escalationPolicyId=mockEscalationPolicyId");
        assertThat(uri.type).isEqualTo("pagerduty");
        assertThat(uri.id).isEqualTo("serviceId");
        assertThat(uri.params).containsEntry("priorityId", "mockPriorityId").containsEntry("escalationPolicyId", "mockEscalationPolicyId");
    }

    @Test
    void notifyWithPagerChannel() {
        var alert = new Alert();
        channelManager.notify("pagerduty://serviceId", alert, 1);

        verify(pagerChannel).notify(eq("serviceId"), anyMap(), eq(alert), eq(1));
    }

    @Test
    void notifyWithDefaultChannel() {
        var alert = new Alert();
        channelManager.notify("channelId", alert, 1);

        verify(slackChannel).notify(eq("channelId"), anyMap(), eq(alert), eq(1));
    }
}
