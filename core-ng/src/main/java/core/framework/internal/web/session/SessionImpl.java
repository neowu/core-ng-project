package core.framework.internal.web.session;

import core.framework.crypto.Hash;
import core.framework.util.Strings;
import core.framework.web.Session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author neo
 */
public class SessionImpl implements Session {
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
        String previousValue = values.put(key, value);
        if (!Strings.equals(previousValue, value)) {
            changedFields.add(key);
        }
    }

    @Override
    public void invalidate() {
        invalidated = true;
    }

    void id(String id) {
        this.id = id;
        hash = Hash.md5Hex(id);
    }

    boolean changed() {
        return !changedFields.isEmpty();
    }
}
