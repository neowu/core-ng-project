package core.framework.impl.web.site;

import core.framework.api.util.Properties;
import core.framework.api.web.Request;
import core.framework.api.web.site.MessageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author neo
 */
public class MessageManager {
    private final Logger logger = LoggerFactory.getLogger(MessageManager.class);
    private final Properties properties = new Properties();
    public MessageProvider messageProvider;

    public void loadProperties(String path) {
        logger.info("load message properties, path={}", path);
        properties.load(path);
    }

    public String message(String key, Request request) {
        if (messageProvider != null) {
            Optional<String> message = messageProvider.get(key, request);
            if (message.isPresent()) return message.get();
        }
        return properties.get(key).orElse(key);
    }
}
