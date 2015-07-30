package core.framework.impl.web.site;

import core.framework.impl.template.function.Function;
import core.framework.impl.web.RequestImpl;

/**
 * @author neo
 */
public class MessageFunction implements Function {
    private final MessageManager messageManager;
    private final RequestImpl request;

    public MessageFunction(MessageManager messageManager, RequestImpl request) {
        this.messageManager = messageManager;
        this.request = request;
    }

    @Override
    public Object apply(Object[] params) {
        String key = String.valueOf(params[0]);
        return messageManager.message(key, request);
    }
}
