package core.framework.impl.web.site;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.Properties;
import core.framework.api.util.Strings;
import core.framework.api.web.site.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class MessageManager implements Message {
    static final String DEFAULT_LANGUAGE = "_default";
    private static final Pattern MESSAGE_PROPERTY_PATH_PATTERN = Pattern.compile("[^_]+((_[a-zA-Z0-9]{2,4})*)\\.properties");
    private final Logger logger = LoggerFactory.getLogger(MessageManager.class);
    public Map<String, List<Properties>> messages;
    String[] languages = new String[]{DEFAULT_LANGUAGE};

    public void load(List<String> paths, String... languages) {
        if (messages != null) throw Exceptions.error("site().message() can only be called once and must before site().template(), please check config");

        messages = Maps.newHashMap();
        if (languages != null) this.languages = languages;
        Map<String, Properties> languageProperties = Maps.newHashMap();

        for (String path : paths) {
            String language = language(path);
            logger.info("load message properties, path={}", path);
            validate(path, language);
            Properties properties = languageProperties.computeIfAbsent(language, key -> new Properties());
            properties.load(path);
        }

        for (String language : this.languages) {
            List<Properties> messages = languageProperties(languageProperties, language);
            this.messages.put(language, messages);
        }
    }

    private List<Properties> languageProperties(Map<String, Properties> languageProperties, String language) {
        List<Properties> messageProperties = Lists.newArrayList();
        String currentLanguage = language;
        while (true) {
            Properties properties = languageProperties.get(currentLanguage);
            if (properties != null) messageProperties.add(properties);
            int index = currentLanguage.lastIndexOf('_');
            if (index < 0) break;
            currentLanguage = currentLanguage.substring(0, index);
        }
        Properties properties = languageProperties.get(DEFAULT_LANGUAGE);
        if (properties != null) messageProperties.add(properties);
        return messageProperties;
    }

    private void validate(String path, String language) {
        if (DEFAULT_LANGUAGE.equals(language)) return;

        if (languages.length == 1 && languages[0].equals(DEFAULT_LANGUAGE)) {
            throw Exceptions.error("language found, but only default language is enabled, path={}, language={}", path, language);
        }

        if (Arrays.stream(languages).noneMatch(enabledLanguage -> enabledLanguage.startsWith(language))) {
            throw Exceptions.error("language does not match enabled languages, path={}, language={}", path, language);
        }
    }

    String language(String path) {
        Matcher matcher = MESSAGE_PROPERTY_PATH_PATTERN.matcher(path);
        if (!matcher.matches())
            throw Exceptions.error("property path must match 'path/name_language.properties' pattern, path={}", path);
        String languagePostfix = matcher.group(1);
        if (Strings.isEmpty(languagePostfix)) return DEFAULT_LANGUAGE;
        return languagePostfix.substring(1);
    }

    @Override
    public Optional<String> get(String key, String language) {
        List<Properties> properties = messages.get(language == null ? DEFAULT_LANGUAGE : language);
        if (properties == null) throw Exceptions.error("language is not defined, please check site().message(), language={}", language);
        for (Properties property : properties) {
            Optional<String> message = property.get(key);
            if (message.isPresent()) return message;
        }
        return Optional.empty();
    }
}
