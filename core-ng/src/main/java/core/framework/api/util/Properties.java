package core.framework.api.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class Properties {
    final Map<String, String> properties = Maps.newHashMap();

    public void load(String path) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new FileNotFoundException("property not found in classpath, classpath=" + path);
            java.util.Properties properties = new java.util.Properties();
            properties.load(stream);
            properties.forEach((key, value) -> {
                String previous = this.properties.putIfAbsent((String) key, (String) value);
                if (previous != null)
                    throw Exceptions.error("property already exists, key={}, previous={}, current={}", key, previous, value);
            });
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
}
