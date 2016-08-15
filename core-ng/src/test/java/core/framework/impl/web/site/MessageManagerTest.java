package core.framework.impl.web.site;

import core.framework.api.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class MessageManagerTest {
    private MessageManager messageManager;

    @Before
    public void createMessageManager() {
        messageManager = new MessageManager();
        List<String> properties = Lists.newArrayList("message-test/messages.properties", "message-test/messages_en.properties", "message-test/messages_en_US.properties");
        messageManager.load(properties, "en", "en_US", "zh");
    }

    @Test
    public void language() {
        assertEquals("en_US", messageManager.language("messages_en_US.properties"));
        assertEquals("en", messageManager.language("messages_en.properties"));

        assertEquals(MessageManager.DEFAULT_LANGUAGE, messageManager.language("messages.properties"));
    }

    @Test
    public void message() {
        assertEquals("value1", messageManager.get("key1", "zh").orElse(null));
        assertEquals("value1", messageManager.get("key1", "en").orElse(null));
        assertEquals("value1", messageManager.get("key1", "en_US").orElse(null));

        assertEquals("value2", messageManager.get("key2", "zh").orElse(null));
        assertEquals("en_value2", messageManager.get("key2", "en").orElse(null));
        assertEquals("en_value2", messageManager.get("key2", "en_US").orElse(null));

        assertEquals("value3", messageManager.get("key3", "zh").orElse(null));
        assertEquals("en_value3", messageManager.get("key3", "en").orElse(null));
        assertEquals("en_US_value3", messageManager.get("key3", "en_US").orElse(null));
    }
}