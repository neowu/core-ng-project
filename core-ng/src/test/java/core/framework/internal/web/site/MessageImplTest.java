package core.framework.internal.web.site;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class MessageImplTest {
    private MessageImpl message;

    @BeforeEach
    void createMessage() {
        message = new MessageImpl();
        message.load(List.of("message-test/messages.properties"));
    }

    @Test
    void get() {
        assertThat(message.get("key1")).isEqualTo("value1");
    }

    @Test
    void getWithNotDefinedLanguage() {
        assertThatThrownBy(() -> message.get("key1", "es_GT"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("language is not defined");
    }
}
