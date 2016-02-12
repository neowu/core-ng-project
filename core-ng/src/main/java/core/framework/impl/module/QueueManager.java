package core.framework.impl.module;

import core.framework.api.module.MessageHandlerConfig;
import core.framework.api.util.Maps;
import core.framework.impl.queue.MessageValidator;

import java.util.Map;

/**
 * @author neo
 */
public class QueueManager {
    private MessageValidator validator;
    private Map<String, MessageHandlerConfig> listeners;

    public Map<String, MessageHandlerConfig> listeners() {
        if (listeners == null) {
            listeners = Maps.newHashMap();
        }
        return listeners;
    }

    public MessageValidator validator() {
        if (validator == null) {
            validator = new MessageValidator();
        }
        return validator;
    }
}
