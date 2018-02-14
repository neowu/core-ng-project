package core.framework.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author neo
 */
public final class Properties {
    final Map<String, String> properties = Maps.newHashMap();

    public void load(String classpath) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath);
        if (stream == null) throw Exceptions.error("can not find property file in classpath, classpath={}", classpath);
        try (Reader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8))) {
            java.util.Properties properties = new java.util.Properties();
            properties.load(reader);
            properties.forEach((key, value) -> set((String) key, (String) value));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<String> get(String key) {
        String value = properties.get(key);
        if (!Strings.isEmpty(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public void set(String key, String value) {
        String previous = this.properties.putIfAbsent(key, value);
        if (previous != null) throw Exceptions.error("property already exists, key={}, previous={}, current={}", key, previous, value);
    }

    public Set<String> keys() {
        return properties.keySet();
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }
}
