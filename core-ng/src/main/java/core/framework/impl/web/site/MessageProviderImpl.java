package core.framework.impl.web.site;

import core.framework.api.util.Properties;
import core.framework.impl.template.MessageProvider;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public class MessageProviderImpl implements MessageProvider {
    private final List<Properties> properties;

    public MessageProviderImpl(List<Properties> properties) {
        this.properties = properties;
    }

    @Override
    public Optional<String> message(String key) {
        for (Properties property : properties) {
            Optional<String> message = property.get(key);
            if (message.isPresent()) return message;
        }
        return Optional.empty();
    }
}
