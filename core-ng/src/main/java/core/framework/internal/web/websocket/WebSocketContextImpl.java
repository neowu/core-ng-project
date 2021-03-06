package core.framework.internal.web.websocket;

import core.framework.web.websocket.Channel;
import core.framework.web.websocket.WebSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author neo
 */
public class WebSocketContextImpl implements WebSocketContext {
    private final Logger logger = LoggerFactory.getLogger(WebSocketContextImpl.class);
    private final Map<String, Map<String, Channel<?>>> rooms = new ConcurrentHashMap<>();
    private final Map<String, Channel<?>> channels = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <V> List<Channel<V>> all() {
        List<Channel<V>> results = new ArrayList<>(channels.size());
        for (Channel<?> channel : channels.values()) {
            results.add((Channel<V>) channel);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> List<Channel<V>> room(String name) {
        Map<String, Channel<?>> channels = rooms.get(name);
        if (channels == null) return List.of();
        List<Channel<V>> results = new ArrayList<>(channels.size());
        for (Channel<?> channel : channels.values()) {
            results.add((Channel<V>) channel);
        }
        return results;
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
            rooms.get(room).remove(channel.id);
        }
    }
}
