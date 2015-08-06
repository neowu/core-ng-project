package core.framework.impl.web.site;

import core.framework.api.web.Request;
import core.framework.impl.template.function.Function;

/**
 * @author neo
 */
public class MessageFunction implements Function {
    private final MessageManager messageManager;
    private final Request request;

    public MessageFunction(MessageManager messageManager, Request request) {
        this.messageManager = messageManager;
        this.request = request;
    }

    @Override
    public Object apply(Object[] params) {
        String key = String.valueOf(params[0]);
        return messageManager.message(key, request);
    }
}
