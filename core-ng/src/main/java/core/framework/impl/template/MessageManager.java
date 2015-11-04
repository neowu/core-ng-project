package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Properties;
import core.framework.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class MessageManager {
    public static final String DEFAULT_LANGUAGE = "default";
    private static final Pattern MESSAGE_PROPERTY_PATH_PATTERN = Pattern.compile("[^_]+((_[a-zA-Z]{2})*)\\.properties");
    private final Logger logger = LoggerFactory.getLogger(MessageManager.class);
    private final Map<String, Properties> messages = Maps.newHashMap();

    public void loadProperties(String path) {
        String language = language(path);
        logger.info("load message properties, path={}", path);
        Properties messages = this.messages.computeIfAbsent(language, key -> new Properties());
        messages.load(path);
    }

    public List<String> languages() {
        return new ArrayList<>(messages.keySet());
    }

    public Optional<String> message(String key, String language) {
        String currentLanguage = language;
        while (true) {
            Properties properties = messages.get(currentLanguage);
            if (properties != null) {
                Optional<String> message = properties.get(key);
                if (message.isPresent()) return message;
            }
            int index = currentLanguage.lastIndexOf('_');
            if (index < 0) break;
            currentLanguage = currentLanguage.substring(0, index);
        }

        Properties properties = messages.get(DEFAULT_LANGUAGE);
        if (properties == null) return Optional.empty();
        return properties.get(key);
    }

    String language(String path) {
        Matcher matcher = MESSAGE_PROPERTY_PATH_PATTERN.matcher(path);
        if (!matcher.matches())
            throw Exceptions.error("property path must follow 'path/name_language.properties' pattern");
        String languagePostfix = matcher.group(1);
        if (Strings.isEmpty(languagePostfix)) return DEFAULT_LANGUAGE;
        return languagePostfix.substring(1);
    }
}
