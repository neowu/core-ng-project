package core.framework.internal.web.websocket;

import core.framework.web.Session;

import java.util.Optional;

/**
 * @author neo
 */
class ReadOnlySession implements Session {
    private final Session session;

    ReadOnlySession(Session session) {
        this.session = session;
    }

    @Override
    public Optional<String> get(String key) {
        return session.get(key);
    }

    @Override
    public void set(String key, String value) {
        throw new Error("session is readonly for websocket");
    }

    @Override
    public void invalidate() {
        throw new Error("session is readonly for websocket");
    }
}
