package core.framework.internal.web.sse;

import core.framework.web.sse.ServerSentEventChannel;
import core.framework.web.sse.ServerSentEventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSentEventContextImpl<T> implements ServerSentEventContext<T> {
    final Map<String, ServerSentEventChannel<T>> channels = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(ServerSentEventContextImpl.class);
    private final Map<String, Map<String, ServerSentEventChannel<T>>> groups = new ConcurrentHashMap<>();

    @Override
    public List<ServerSentEventChannel<T>> all() {
        // "new ArrayList(Collection)" doesn't check null element, so it's faster than List.copyOf
        return new ArrayList<>(channels.values());
    }

    @Override
    public List<ServerSentEventChannel<T>> group(String name) {
        Map<String, ServerSentEventChannel<T>> channels = groups.get(name);
        if (channels == null) return List.of();
        return new ArrayList<>(channels.values());
    }

    void join(ServerSentEventChannelImpl<T> channel, String group) {
        logger.debug("join group, channel={}, group={}", channel.id, group);
        channel.groups.add(group);
        groups.computeIfAbsent(group, key -> new ConcurrentHashMap<>()).put(channel.id, channel);
    }

    void leave(ServerSentEventChannelImpl<T> channel, String group) {
        logger.debug("leave group, channel={}, group={}", channel.id, group);
        channel.groups.remove(group);
        Map<String, ServerSentEventChannel<T>> channels = groups.get(group);
        if (channels != null) channels.remove(channel.id);
    }

    void add(ServerSentEventChannelImpl<T> channel) {
        channels.put(channel.id, channel);
    }

    void remove(ServerSentEventChannelImpl<?> channel) {
        channels.remove(channel.id);
        for (String group : channel.groups) {
            Map<String, ServerSentEventChannel<T>> groupChannels = groups.get(group);
            groupChannels.remove(channel.id);

            // cleanup group if it has no channels, and thread safe
            if (groupChannels.isEmpty()) {
                var previous = groups.remove(group);
                // in case another channel was added before removal by another thread
                if (!previous.isEmpty()) groups.computeIfAbsent(group, key -> new ConcurrentHashMap<>()).putAll(previous);
            }
        }
    }
}
