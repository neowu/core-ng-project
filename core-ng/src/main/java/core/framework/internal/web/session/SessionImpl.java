package core.framework.internal.web.session;

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
    String id;
    public final String domain;
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

    boolean changed() {
        return !changedFields.isEmpty();
    }

    @Override
    public void invalidate() {
        invalidated = true;
    }
}
