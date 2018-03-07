package core.framework.impl.module;

import core.framework.util.Exceptions;
import core.framework.util.Maps;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author neo
 */
public class Config {
    private final Map<String, Object> states = Maps.newHashMap();

    public <T> T state(String key) {
        @SuppressWarnings("unchecked")
        T state = (T) states.get(key);
        if (state == null) throw Exceptions.error("{} is not configured", key);
        return state;
    }

    public <T> T state(String key, Supplier<T> constructor) {
        @SuppressWarnings("unchecked")
        T state = (T) states.computeIfAbsent(key, k -> constructor.get());
        return state;
    }

    public void validate() {
        states.values().stream().filter(State.class::isInstance)
              .map(State.class::cast)
              .forEach(State::validate);
    }

    public interface State {
        void validate();
    }
}
