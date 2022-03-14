package core.framework.log.message;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionLogMessageTest {
    @Test
    void firstContextValue() {
        ActionLogMessage message = new ActionLogMessage();
        message.context = Map.of("key", List.of("value1", "value2"));

        assertThat(message.firstContextValue("key")).isEqualTo("value1");
        assertThat(message.firstContextValue("notExisted")).isNull();
    }
}
