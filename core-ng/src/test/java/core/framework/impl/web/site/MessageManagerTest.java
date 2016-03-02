package core.framework.impl.web.site;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class MessageManagerTest {
    private MessageManager messageManager;

    @Before
    public void createMessageManager() {
        messageManager = new MessageManager();
        messageManager.loadProperties("message-test/messages.properties");
        messageManager.loadProperties("message-test/messages_en.properties");
        messageManager.loadProperties("message-test/messages_en_US.properties");
    }

    @Test
    public void language() {
        assertEquals("en_US", messageManager.language("messages_en_US.properties"));
        assertEquals("en", messageManager.language("messages_en.properties"));

        assertEquals(MessageManager.DEFAULT_LANGUAGE, messageManager.language("messages.properties"));
    }

    @Test
    public void effectiveLanguage() {
        assertEquals(MessageManager.DEFAULT_LANGUAGE, messageManager.effectiveLanguage("zh"));
        assertEquals("en_US", messageManager.effectiveLanguage("en_US"));
        assertEquals("en", messageManager.effectiveLanguage("en"));
        assertEquals("en", messageManager.effectiveLanguage("en_CA"));
    }

    @Test
    public void message() {
        assertEquals("value1", message("zh", "key1"));
        assertEquals("value1", message("en", "key1"));
        assertEquals("value1", message("en_US", "key1"));

        assertEquals("value2", message("zh", "key2"));
        assertEquals("en_value2", message("en", "key2"));
        assertEquals("en_value2", message("en_US", "key2"));

        assertEquals("value3", message("zh", "key3"));
        assertEquals("en_value3", message("en", "key3"));
        assertEquals("en_US_value3", message("en_US", "key3"));
    }

    private String message(String language, String key) {
        return messageManager.messageProvider(messageManager.effectiveLanguage(language)).message(key).get();
    }
}