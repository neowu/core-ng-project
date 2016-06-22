package core.framework.impl.web.session;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.web.Session;

import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class SessionImpl implements Session {
    private static final int MAX_VALUE_LENGTH = 500;
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
        if (value.length() > MAX_VALUE_LENGTH)
            throw Exceptions.error("the length value must not be larger than {}, length={}", MAX_VALUE_LENGTH, value.length());
        data.put(key, value);
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
