package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class MessageManager {
    public static final String DEFAULT_LANGUAGE = "default";

    private final Logger logger = LoggerFactory.getLogger(MessageManager.class);
    private final Map<String, Properties> messages = Maps.newHashMap();

    public void loadProperties(String path) {
        if (!path.endsWith(".properties")) throw Exceptions.error("path must end with .properties");
        logger.info("load message properties, path={}", path);
        String language = language(path);
        Properties messages = this.messages.computeIfAbsent(language, key -> new Properties());
        messages.load(path);
    }

    public List<String> languages() {
        return new ArrayList<>(messages.keySet());
    }

    public Optional<String> message(String key, String language) {
        Properties properties = messages.get(language);
        if (properties == null) throw Exceptions.error("can not find messages, language={}", language);
        return properties.get(key);
    }

    String language(String path) {
        int length = path.length();

        // check whether follow base_xx_xx.properties
        if (length > 17
            && path.charAt(length - 17) == '_'
            && Character.isAlphabetic(path.charAt(length - 16))
            && Character.isAlphabetic(path.charAt(length - 15))
            && path.charAt(length - 14) == '_'
            && Character.isAlphabetic(path.charAt(length - 13))
            && Character.isAlphabetic(path.charAt(length - 12))) {
            return path.substring(length - 16, length - 11);
        }

        return DEFAULT_LANGUAGE;
    }
}
