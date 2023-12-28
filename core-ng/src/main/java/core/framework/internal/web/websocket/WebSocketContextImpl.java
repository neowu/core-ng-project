package core.framework.internal.web.websocket;

import core.framework.web.websocket.Channel;
import core.framework.web.websocket.WebSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author neo
 */
public class WebSocketContextImpl implements WebSocketContext {
    private final Logger logger = LoggerFactory.getLogger(WebSocketContextImpl.class);
    private final Map<String, Channel<?>> channels = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Channel<?>>> rooms = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <V> List<Channel<V>> all() {
        // "new ArrayList(Collection)" doesn't check null element, so it's faster than List.copyOf
        return (List<Channel<V>>) new ArrayList<>((Collection<?>) channels.values());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> List<Channel<V>> room(String name) {
        Map<String, Channel<?>> channels = rooms.get(name);
        if (channels == null) return List.of();
        return (List<Channel<V>>) new ArrayList<>((Collection<?>) channels.values());
    }

    void join(ChannelImpl<?, ?> channel, String room) {
        logger.debug("join room, channel={}, room={}", channel.id, room);
        channel.rooms.add(room);
        rooms.computeIfAbsent(room, key -> new ConcurrentHashMap<>()).put(channel.id, channel);
    }

    void leave(ChannelImpl<?, ?> channel, String room) {
        logger.debug("leave room, channel={}, room={}", channel.id, room);
        channel.rooms.remove(room);
        Map<String, Channel<?>> channels = rooms.get(room);
        if (channels != null) channels.remove(channel.id);
    }

    void add(ChannelImpl<?, ?> channel) {
        channels.put(channel.id, channel);
    }

    void remove(ChannelImpl<?, ?> channel) {
        channels.remove(channel.id);
        for (String room : channel.rooms) {
            Map<String, Channel<?>> roomChannels = rooms.get(room);
            roomChannels.remove(channel.id);
            if (roomChannels.isEmpty()) rooms.remove(room); // cleanup room if it has no channels
        }
    }
}
