package core.framework.impl.web.site;

import core.framework.util.Exceptions;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.util.Properties;
import core.framework.util.Sets;
import core.framework.util.Strings;
import core.framework.web.site.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class MessageImpl implements Message {
    static final String DEFAULT_LANGUAGE = "_default";
    private static final Pattern MESSAGE_PROPERTY_PATH_PATTERN = Pattern.compile("[^_]+((_[a-zA-Z0-9]{2,4})*)\\.properties");
    private final Logger logger = LoggerFactory.getLogger(MessageImpl.class);
    private final Map<String, List<Properties>> messages = Maps.newHashMap();
    String[] languages = new String[]{DEFAULT_LANGUAGE};

    public void load(List<String> paths, String... languages) {
        if (!messages.isEmpty()) throw new Error("messages must be empty");

        if (languages.length > 0) this.languages = languages;
        Map<String, Properties> properties = Maps.newHashMap();

        for (String path : paths) {
            logger.info("load message properties, path={}", path);
            String language = language(path);
            validateLanguage(path, language);
            properties.computeIfAbsent(language, key -> new Properties())
                      .load(path);
        }

        for (String language : this.languages) {
            List<Properties> messages = languageProperties(properties, language);
            this.messages.put(language, messages);
        }

        validateMessageKeys();
    }

    private void validateMessageKeys() {
        Map<String, Set<String>> allLanguageKeys = Maps.newHashMap();
        Set<String> allKeys = Sets.newHashSet();
        messages.forEach((language, languageProperties) -> {
            Set<String> languageKeys = Sets.newHashSet();
            languageProperties.forEach(properties -> languageKeys.addAll(properties.keys()));
            allKeys.addAll(languageKeys);
            allLanguageKeys.put(language, languageKeys);
        });
        allLanguageKeys.forEach((language, keys) -> {
            if (!allKeys.equals(keys)) {
                Set<String> missingKeys = new HashSet<>(allKeys);
                missingKeys.removeAll(keys);
                throw Exceptions.error("message keys are missing for language, language={}, keys={}", language, missingKeys);
            }
        });
    }

    private void validateLanguage(String path, String language) {
        if (DEFAULT_LANGUAGE.equals(language)) return;

        if (languages.length == 1 && languages[0].equals(DEFAULT_LANGUAGE)) {
            throw Exceptions.error("language found, but only default language is enabled, path={}, language={}", path, language);
        }

        if (Arrays.stream(languages).noneMatch(enabledLanguage -> enabledLanguage.startsWith(language))) {
            throw Exceptions.error("language does not match enabled languages, path={}, language={}", path, language);
        }
    }

    private List<Properties> languageProperties(Map<String, Properties> languageProperties, String language) {
        List<Properties> messages = Lists.newArrayList();
        String currentLanguage = language;
        while (true) {
            Properties properties = languageProperties.get(currentLanguage);
            if (properties != null) messages.add(properties);
            int index = currentLanguage.lastIndexOf('_');
            if (index < 0) break;
            currentLanguage = currentLanguage.substring(0, index);
        }
        Properties properties = languageProperties.get(DEFAULT_LANGUAGE);
        if (properties != null) messages.add(properties);
        return messages;
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
        String targetLanguage = language == null ? DEFAULT_LANGUAGE : language;
        List<Properties> properties = messages.get(targetLanguage);
        if (properties == null) throw Exceptions.error("language is not defined, please check site().message(), language={}", targetLanguage);
        for (Properties property : properties) {
            Optional<String> message = property.get(key);
            if (message.isPresent()) return message;
        }
        return Optional.empty();
    }
}
