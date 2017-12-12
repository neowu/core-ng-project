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
        PropertyEntry value = propertyValue(key);
        if (value.override) {
            if (!properties.containsKey(key))
                throw Exceptions.error("-D{}={} must override property file, please add key to property file", key, value.maskedValue());
            logger.info("found overridden property by -D{}={}", key, value.maskedValue());
        }
        return value.value();
    }

    public List<PropertyEntry> entries() {
        Set<String> keys = new TreeSet<>(this.properties.keys());   // sort by key
        return keys.stream().map(this::propertyValue).collect(Collectors.toList());
    }

    private PropertyEntry propertyValue(String key) {
        String value = System.getProperty(key);     // allow use system property to overwrite values in property file, e.g. -Dsys.http.port=8080
        if (!Strings.isEmpty(value)) {
            return new PropertyEntry(key, value, true);
        } else {
            return new PropertyEntry(key, this.properties.get(key).orElse(""), false);
        }
    }

    public static class PropertyEntry {
        public final String key;
        public final String value;
        public final boolean override;

        PropertyEntry(String key, String value, boolean override) {
            this.key = key;
            this.value = value;
            this.override = override;
        }

        Optional<String> value() {
            if (Strings.isEmpty(value)) return Optional.empty();
            return Optional.of(value);
        }

        public String maskedValue() {
            if (key.contains("password") || key.contains("secret")) return "(masked)";
            return value;
        }
    }
}
