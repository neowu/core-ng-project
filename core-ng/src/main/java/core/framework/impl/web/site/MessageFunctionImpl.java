package core.framework.impl.web.site;

import core.framework.api.web.Request;
import core.framework.impl.template.MessageFunction;

/**
 * @author neo
 */
public class MessageFunctionImpl implements MessageFunction {
    private final MessageManager messageManager;
    private final Request request;

    public MessageFunctionImpl(MessageManager messageManager, Request request) {
        this.messageManager = messageManager;
        this.request = request;
    }

    @Override
    public String message(String key) {
        return messageManager.message(key, request);
    }
}
