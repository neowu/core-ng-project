package core.log.kafka;

import core.framework.json.JSON;
import core.framework.log.message.ActionLogMessage;
import core.framework.util.ClasspathResources;
import core.log.LogFilterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionFilterTest {
    private ActionFilter filter;

    @BeforeEach
    void createActionFilter() {
        LogFilterConfig config = JSON.fromJSON(LogFilterConfig.class, ClasspathResources.text("filter.json"));
        filter = new ActionFilter(config.action);
    }

    @Test
    void ignoreTrace() {
        var message = new ActionLogMessage();
        message.app = "website";
        assertThat(filter.ignoreTrace(message)).isFalse();
        message.errorCode = "REQUEST_BLOCKED";
        assertThat(filter.ignoreTrace(message)).isTrue();
        message.errorCode = "NOT_FOUND";
        assertThat(filter.ignoreTrace(message)).isFalse();
    }
}
