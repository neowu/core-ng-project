package core.framework.internal.module;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
public class ServiceRegistry {
    public final Set<Class<?>> serviceInterfaces = new LinkedHashSet<>();
    public final Set<Class<?>> beanClasses = new LinkedHashSet<>();  // custom bean classes not referred by service interfaces, e.g. via controller, to publish via /_sys/api
    public final List<MessagePublish> messages = new ArrayList<>();

    public static class MessagePublish {
        public final String topic;  // topic can be null, for dynamic topic message publish
        public final Class<?> messageClass;

        public MessagePublish(String topic, Class<?> messageClass) {
            this.topic = topic;
            this.messageClass = messageClass;
        }
    }
}
