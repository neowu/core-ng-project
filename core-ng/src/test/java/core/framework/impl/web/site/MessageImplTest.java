package core.framework.impl.web.site;

import core.framework.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class MessageImplTest {
    private MessageImpl message;

    @BeforeEach
    void createMessage() {
        message = new MessageImpl();
        List<String> properties = Lists.newArrayList("message-test/messages.properties", "message-test/messages_en.properties", "message-test/messages_en_US.properties");
        message.load(properties, "en", "en_US", "zh");
    }

    @Test
    void language() {
        assertEquals("en_US", message.language("messages_en_US.properties"));
        assertEquals("en", message.language("messages_en.properties"));

        assertEquals(MessageImpl.DEFAULT_LANGUAGE, message.language("messages.properties"));
    }

    @Test
    void message() {
        assertEquals("value1", message.get("key1", "zh").orElse(null));
        assertEquals("value1", message.get("key1", "en").orElse(null));
        assertEquals("value1", message.get("key1", "en_US").orElse(null));

        assertEquals("value2", message.get("key2", "zh").orElse(null));
        assertEquals("en_value2", message.get("key2", "en").orElse(null));
        assertEquals("en_value2", message.get("key2", "en_US").orElse(null));

        assertEquals("value3", message.get("key3", "zh").orElse(null));
        assertEquals("en_value3", message.get("key3", "en").orElse(null));
        assertEquals("en_US_value3", message.get("key3", "en_US").orElse(null));
    }
}
