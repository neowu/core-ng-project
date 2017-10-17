package core.framework.impl.inject;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author neo
 */
public final class Key {
    public final Type type;
    public final String name;

    public Key(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Key key = (Key) object;
        return Objects.equals(type, key.type)
                && Objects.equals(name, key.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return "Key{type=" + type + ", name=" + name + '}';
    }
}
