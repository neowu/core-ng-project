package core.framework.internal.web.session;

import core.framework.crypto.Hash;
import core.framework.util.Strings;
import core.framework.web.Session;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author neo
 */
public class SessionImpl implements Session {
    static final String TIMEOUT_FIELD = "_timeout";     // reserved field for custom timeout

    final Map<String, String> values = new HashMap<>();
    final Set<String> changedFields = new HashSet<>();
    final String domain;
    String id;
    // used to track current session, without exposing actual session id value
    // redis session store use sha256(domain+sessionId) to hash, here to use md5 to make sure not logging actual redis key, and keep reference shorter
    String hash;
    boolean invalidated;
    boolean saved;

    public SessionImpl(String domain) {
        this.domain = domain;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public void set(String key, String value) {
        if (Strings.startsWith(key, '_')) throw new Error("key must not start with '_', key=" + key);
        internalSet(key, value);
    }

    @Override
    public void invalidate() {
        invalidated = true;
    }

    @Override
    public void timeout(Duration timeout) {
        // timeout.toSeconds can be zero or negative, which is allowed by local/redis store
        internalSet(TIMEOUT_FIELD, String.valueOf(timeout.toSeconds()));
    }

    private void internalSet(String key, String value) {
        String previousValue = values.put(key, value);
        if (!Strings.equals(previousValue, value)) {
            changedFields.add(key);
        }
    }

    void id(String id) {
        this.id = id;
        hash = Hash.md5Hex(id);
    }

    boolean changed() {
        return !changedFields.isEmpty();
    }
}
