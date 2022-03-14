package core.framework.module;

import core.framework.internal.web.site.MessageImpl;
import core.framework.internal.web.site.MockMessage;
import core.framework.web.site.Message;

/**
 * @author neo
 */
public class TestSiteConfig extends SiteConfig {
    @Override
    Message message(MessageImpl message) {
        return new MockMessage(message);
    }
}
