package core.framework.internal.web.site;

import core.framework.web.site.Message;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public class MockMessage implements Message {
    private final MessageImpl message;

    public MockMessage(MessageImpl message) {
        this.message = message;
    }

    @Override
    public String get(String key, @Nullable String language) {
        return message.getMessage(key, language).orElseThrow(() -> new Error("can not find message, key=" + key));
    }
}
