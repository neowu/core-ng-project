package core.framework.impl.template;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class MessageManagerTest {
    MessageManager messageManager;

    @Before
    public void createMessageManager() {
        messageManager = new MessageManager();
    }

    @Test
    public void language() {
        Assert.assertEquals("en_US", messageManager.language("messages_en_US.properties"));
        Assert.assertEquals("en", messageManager.language("messages_en.properties"));

        Assert.assertEquals(MessageManager.DEFAULT_LANGUAGE, messageManager.language("messages.properties"));
    }

    @Test
    public void message() {
        messageManager.loadProperties("message-test/messages.properties");
        messageManager.loadProperties("message-test/messages_en.properties");
        messageManager.loadProperties("message-test/messages_en_US.properties");

        Assert.assertEquals("value1", messageManager.message("key1", "zh").get());
        Assert.assertEquals("value1", messageManager.message("key1", "en").get());
        Assert.assertEquals("value1", messageManager.message("key1", "en_US").get());

        Assert.assertEquals("value2", messageManager.message("key2", "zh").get());
        Assert.assertEquals("en_value2", messageManager.message("key2", "en").get());
        Assert.assertEquals("en_value2", messageManager.message("key2", "en_US").get());

        Assert.assertEquals("value3", messageManager.message("key3", "zh").get());
        Assert.assertEquals("en_value3", messageManager.message("key3", "en").get());
        Assert.assertEquals("en_US_value3", messageManager.message("key3", "en_US").get());
    }
}