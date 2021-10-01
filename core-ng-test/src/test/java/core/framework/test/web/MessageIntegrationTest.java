package core.framework.test.web;

import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import core.framework.web.site.Message;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class MessageIntegrationTest extends IntegrationTest {
    @Inject
    Message message;

    @Test
    void getWithNotExistedKey() {
        assertThatThrownBy(() -> message.get("notExistedKey"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("can not find message");
    }
}
