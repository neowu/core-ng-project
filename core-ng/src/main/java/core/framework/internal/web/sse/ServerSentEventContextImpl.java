package core.framework.internal.web.sse;

import core.framework.web.sse.Channel;
import core.framework.web.sse.ServerSentEventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSentEventContextImpl<T> implements ServerSentEventContext<T> {
    final Map<String, Channel<T>> channels = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(ServerSentEventContextImpl.class);
    private final Map<String, Map<String, Channel<T>>> groups = new ConcurrentHashMap<>();

    @Override
    public List<Channel<T>> all() {
        // "new ArrayList(Collection)" doesn't check null element, so it's faster than List.copyOf
        return new ArrayList<>(channels.values());
    }

    @Override
    public List<Channel<T>> group(String name) {
        Map<String, Channel<T>> channels = groups.get(name);
        if (channels == null) return List.of();
        return new ArrayList<>(channels.values());
    }

    void join(ChannelImpl<T> channel, String group) {
        logger.debug("join group, channel={}, group={}", channel.id, group);
        channel.groups.add(group);
        groups.computeIfAbsent(group, key -> new ConcurrentHashMap<>()).put(channel.id, channel);
    }

    void leave(ChannelImpl<T> channel, String group) {
        logger.debug("leave group, channel={}, group={}", channel.id, group);
        channel.groups.remove(group);
        Map<String, Channel<T>> channels = groups.get(group);
        if (channels != null) channels.remove(channel.id);
    }

    void add(ChannelImpl<T> channel) {
        channels.put(channel.id, channel);
    }

    void remove(ChannelImpl<?> channel) {
        channels.remove(channel.id);
        for (String group : channel.groups) {
            Map<String, Channel<T>> groupChannels = groups.get(group);
            groupChannels.remove(channel.id);

            // cleanup group if it has no channels, and thread safe
            if (groupChannels.isEmpty()) {
                var previous = groups.remove(group);
                // in case another channel was added before removal by another thread
                if (!previous.isEmpty()) groups.computeIfAbsent(group, key -> new ConcurrentHashMap<>()).putAll(previous);
            }
        }
    }

    public void keepAlive() {
        logger.info("keepalive sse connections");
        long now = System.nanoTime();
        for (Channel<T> channel : channels.values()) {
            ChannelImpl<?> impl = (ChannelImpl<?>) channel;
            if (now - impl.lastSentTime >= 15_000_000_000L) {
                impl.send(":\n");
            }
        }
    }
}
