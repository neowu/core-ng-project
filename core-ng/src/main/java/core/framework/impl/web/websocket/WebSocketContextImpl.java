package core.framework.impl.web.websocket;

import core.framework.util.Sets;
import core.framework.web.websocket.Channel;
import core.framework.web.websocket.WebSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author neo
 */
public class WebSocketContextImpl implements WebSocketContext {
    private final Logger logger = LoggerFactory.getLogger(WebSocketContextImpl.class);
    private final Map<String, Set<Channel>> rooms = new ConcurrentHashMap<>();

    @Override
    public Set<Channel> room(String name) {
        Set<Channel> channels = rooms.get(name);
        if (channels == null) return Set.of();
        return channels;
    }

    void join(ChannelImpl channel, String room) {
        logger.debug("join room, channel={}, room={}", channel.id, room);
        channel.rooms.add(room);
        rooms.computeIfAbsent(room, key -> Sets.newConcurrentHashSet()).add(channel);
    }

    void leave(ChannelImpl channel, String room) {
        logger.debug("leave room, channel={}, room={}", channel.id, room);
        channel.rooms.remove(room);
        rooms.computeIfAbsent(room, key -> Sets.newConcurrentHashSet()).remove(channel);
    }

    void remove(ChannelImpl channel) {
        for (String room : channel.rooms) {
            rooms.get(room).remove(channel);
        }
    }
}
