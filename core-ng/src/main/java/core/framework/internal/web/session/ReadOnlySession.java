package core.framework.internal.web.session;

import core.framework.web.Session;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * @author neo
 */
public final class ReadOnlySession implements Session {
    @Nullable
    public static ReadOnlySession of(@Nullable Session session) {
        if (session == null) return null;
        return new ReadOnlySession(session);
    }

    private final Session session;

    private ReadOnlySession(Session session) {
        this.session = session;
    }

    @Override
    public Optional<String> get(String key) {
        return session.get(key);
    }

    @Override
    public void set(String key, String value) {
        throw new Error("session is readonly");
    }

    @Override
    public void invalidate() {
        throw new Error("session is readonly");
    }
}
