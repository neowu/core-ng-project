package core.framework.internal.web.site;

import core.framework.log.Markers;
import core.framework.util.Maps;
import core.framework.util.Properties;
import core.framework.util.Sets;
import core.framework.util.Strings;
import core.framework.web.site.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class MessageImpl implements Message {
    static final String DEFAULT_LANGUAGE = "_default";
    private static final Pattern MESSAGE_PROPERTY_PATH_PATTERN = Pattern.compile("[^_]+((_[a-zA-Z0-9]{2,4})*)\\.properties");
    final Map<String, List<Properties>> messages = Maps.newHashMap();
    private final Logger logger = LoggerFactory.getLogger(MessageImpl.class);
    String[] languages = {DEFAULT_LANGUAGE};

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

    void validateMessageKeys() {
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
                throw new Error(format("message keys are missing for language, language={}, keys={}", language, missingKeys));
            }
        });
    }

    private void validateLanguage(String path, String language) {
        if (DEFAULT_LANGUAGE.equals(language)) return;

        if (languages.length == 1 && DEFAULT_LANGUAGE.equals(languages[0])) {
            throw new Error(format("found language specific messages, but only default language is enabled, path={}, language={}", path, language));
        }

        if (Arrays.stream(languages).noneMatch(enabledLanguage -> enabledLanguage.startsWith(language))) {
            throw new Error(format("language does not match enabled languages, path={}, language={}", path, language));
        }
    }

    private List<Properties> languageProperties(Map<String, Properties> languageProperties, String language) {
        List<Properties> messages = new ArrayList<>(languageProperties.size());
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
            throw new Error("property path must match 'path/name_language.properties' pattern, path=" + path);
        String languagePostfix = matcher.group(1);
        if (Strings.isBlank(languagePostfix)) return DEFAULT_LANGUAGE;
        return languagePostfix.substring(1);
    }

    @Override
    public String get(String key, String language) {
        String message = getMessage(key, language).orElse(null);
        if (message == null) {
            logger.error(Markers.errorCode("INVALID_MESSAGE_KEY"), "can not find message, key={}, language={}", key, language);
            return key;
        }
        return message;
    }

    Optional<String> getMessage(String key, String language) {
        String targetLanguage = language == null ? languages[0] : language;
        List<Properties> properties = messages.get(targetLanguage);
        if (properties == null) throw new Error("language is not defined, please check site().message(), language=" + targetLanguage);
        for (Properties property : properties) {
            Optional<String> message = property.get(key);
            if (message.isPresent()) return message;
        }
        return Optional.empty();
    }
}
