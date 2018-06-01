package core.framework.impl.web.site;

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
        List<String> properties = List.of("message-test/messages.properties", "message-test/messages_en.properties", "message-test/messages_en_US.properties");
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
        assertEquals("value1", message.get("key1", "zh").orElseThrow());
        assertEquals("value1", message.get("key1", "en").orElseThrow());
        assertEquals("value1", message.get("key1", "en_US").orElseThrow());

        assertEquals("value2", message.get("key2", "zh").orElseThrow());
        assertEquals("en_value2", message.get("key2", "en").orElseThrow());
        assertEquals("en_value2", message.get("key2", "en_US").orElseThrow());

        assertEquals("value3", message.get("key3", "zh").orElseThrow());
        assertEquals("en_value3", message.get("key3", "en").orElseThrow());
        assertEquals("en_US_value3", message.get("key3", "en_US").orElseThrow());
    }
}
