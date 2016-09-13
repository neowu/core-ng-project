package core.framework.impl.web.session;

import core.framework.api.util.Maps;
import core.framework.api.web.Session;

import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class SessionImpl implements Session {
    final Map<String, String> data = Maps.newHashMap();
    String id;
    boolean changed;
    boolean invalidated;

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    @Override
    public void set(String key, String value) {
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
        changed = true;
    }

    @Override
    public void remove(String key) {
        data.remove(key);
        changed = true;
    }

    @Override
    public void invalidate() {
        invalidated = true;
    }
}
