package app.monitor.channel;

import app.monitor.alert.Alert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

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
        ChannelURI uriWithType = channelManager.parseChannelURI("slack://channelId");
        Assertions.assertEquals("slack", uriWithType.type);
        Assertions.assertEquals("channelId", uriWithType.id);

        ChannelURI uri = channelManager.parseChannelURI("channelId");
        Assertions.assertNull(uri.type);
        Assertions.assertEquals("channelId", uri.id);

        ChannelURI uriWithTypeAndParams = channelManager.parseChannelURI("pagerduty://serviceId?priorityId=mockPriorityId&escalationPolicyId=mockEscalationPolicyId");
        Assertions.assertEquals("pagerduty", uriWithTypeAndParams.type);
        Assertions.assertEquals("serviceId", uriWithTypeAndParams.id);
        Assertions.assertEquals("mockPriorityId", uriWithTypeAndParams.params.get("priorityId"));
        Assertions.assertEquals("mockEscalationPolicyId", uriWithTypeAndParams.params.get("escalationPolicyId"));
    }

    @Test
    void notifyWithPagerChannel() {
        var alert = new Alert();
        channelManager.notify("pager://channelId", alert, 1);

        verify(pagerChannel).notify("channelId", null, alert, 1);
    }

    @Test
    void notifyWithDefaultChannel() {
        var alert = new Alert();
        channelManager.notify("channelId", alert, 1);

        verify(slackChannel).notify("channelId", null, alert, 1);
    }
}
