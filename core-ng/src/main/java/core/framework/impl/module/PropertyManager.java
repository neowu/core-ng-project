package core.framework.impl.module;

import core.framework.util.ASCII;
import core.framework.util.Properties;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author neo
 */
public class PropertyManager {
    public final Properties properties = new Properties();
    private final Logger logger = LoggerFactory.getLogger(PropertyManager.class);

    public Optional<String> property(String key) {
        String envVarName = envVarName(key);
        String value = System.getenv(envVarName);  // use env var to override property, e.g. under docker/kubenetes, SYS_HTTP_PORT to override sys.http.port
        if (!Strings.isEmpty(value)) {
            logger.info("found overridden property by env var {}, key={}, value={}", envVarName, key, maskValue(key, value));
            return Optional.of(value);
        }
        value = System.getProperty(key);     // use system property to override property, e.g. -Dsys.http.port=8080
        if (!Strings.isEmpty(value)) {
            logger.info("found overridden property by system property -D{}, key={}, value={}", key, key, maskValue(key, value));
            return Optional.of(value);
        }
        return properties.get(key);
    }

    public String maskValue(String key, String value) {
        if (key.contains("password") || key.contains("secret")) return "******";
        return value;
    }

    String envVarName(String propertyKey) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < propertyKey.length(); i++) {
            char ch = propertyKey.charAt(i);
            if (ch == '.') builder.append('_');
            else builder.append(ASCII.toUpperCase(ch));
        }
        return builder.toString();
    }
}
