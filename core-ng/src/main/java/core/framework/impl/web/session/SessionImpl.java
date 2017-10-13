package core.framework.impl.web.session;

import core.framework.util.Maps;
import core.framework.util.Sets;
import core.framework.web.Session;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author neo
 */
public class SessionImpl implements Session {
    final Map<String, String> values = Maps.newHashMap();
    final Set<String> changedFields = Sets.newHashSet();
    String id;
    boolean invalidated;

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public void set(String key, String value) {
        values.put(key, value);
        changedFields.add(key);
    }

    @Override
    public void remove(String key) {
        values.put(key, null);
        changedFields.add(key);
    }

    boolean changed() {
        return !changedFields.isEmpty();
    }

    @Override
    public void invalidate() {
        invalidated = true;
    }
}
