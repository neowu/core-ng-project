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
        Assert.assertEquals(MessageManager.DEFAULT_LANGUAGE_KEY, messageManager.language("messages.properties"));
        Assert.assertEquals(MessageManager.DEFAULT_LANGUAGE_KEY, messageManager.language("m.properties"));
    }
}