package core.framework.impl.module;

import core.framework.util.Exceptions;
import core.framework.util.Properties;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class PropertyManager {
    public final Properties properties = new Properties();
    private final Logger logger = LoggerFactory.getLogger(PropertyManager.class);

    public Optional<String> property(String key) {
        PropertyEntry entry = entry(key);
        if (entry.source != PropertySource.PROPERTY_FILE) {
            if (!properties.containsKey(key)) throw Exceptions.error("key defined in env variable or system property must override property file, please add key to property file, key={}, value={}", key, entry.maskedValue());
            logger.info("found overridden property, key={}, value={}, source={}", key, entry.maskedValue(), entry.source);
        }
        return entry.value();
    }

    public List<PropertyEntry> entries() {
        Set<String> keys = new TreeSet<>(this.properties.keys());   // sort by key
        return keys.stream().map(this::entry).collect(Collectors.toList());
    }

    private PropertyEntry entry(String key) {
        String value = System.getenv(key);  // allow use env variable to overwrite values in property file, e.g. under docker/kubenetes
        if (!Strings.isEmpty(value)) return new PropertyEntry(key, value, PropertySource.ENV_VAR);
        value = System.getProperty(key);     // allow use system property to overwrite values in property file, e.g. -Dsys.http.port=8080
        if (!Strings.isEmpty(value)) return new PropertyEntry(key, value, PropertySource.SYSTEM_PROPERTY);
        return new PropertyEntry(key, this.properties.get(key).orElse(null), PropertySource.PROPERTY_FILE);
    }

    public enum PropertySource {
        ENV_VAR, SYSTEM_PROPERTY, PROPERTY_FILE
    }

    public static class PropertyEntry {
        public final String key;
        public final String value;
        public final PropertySource source;

        PropertyEntry(String key, String value, PropertySource source) {
            this.key = key;
            this.value = value;
            this.source = source;
        }

        Optional<String> value() {
            return Optional.ofNullable(value);
        }

        public String maskedValue() {
            if (key.contains("password") || key.contains("secret")) return "(masked)";
            return value;
        }
    }
}
