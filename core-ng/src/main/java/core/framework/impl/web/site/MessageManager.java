package core.framework.impl.web.site;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.Properties;
import core.framework.api.util.Strings;
import core.framework.impl.template.MessageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class MessageManager {
    static final String DEFAULT_LANGUAGE = "_default";
    private static final Pattern MESSAGE_PROPERTY_PATH_PATTERN = Pattern.compile("[^_]+((_[a-zA-Z0-9]{2,4})*)\\.properties");
    private final Map<String, Properties> messages = Maps.newHashMap();
    private final Logger logger = LoggerFactory.getLogger(MessageManager.class);
    public String[] languages = new String[]{DEFAULT_LANGUAGE};

    public void load(String path) {
        String language = language(path);
        logger.info("load message properties, path={}", path);
        Properties messages = this.messages.computeIfAbsent(language, key -> new Properties());
        messages.load(path);
    }

    public void validate() {
        if (languages.length == 1 && DEFAULT_LANGUAGE.equals(languages[0]) && messages.keySet().stream().anyMatch(language -> !language.equals(DEFAULT_LANGUAGE)))
            throw Exceptions.error("site().template().language() must be called before site().template().add() if language specific message loaded");

        messages.keySet().stream()
                .filter(effectiveLanguage -> !DEFAULT_LANGUAGE.equals(effectiveLanguage)
                    && Arrays.stream(languages).noneMatch(language -> language.startsWith(effectiveLanguage)))
                .forEach(effectiveLanguage -> {
                    throw Exceptions.error("message loaded by site().template().message() but not used in enabled languages, language={}", effectiveLanguage);
                });
    }

    String language(String path) {
        Matcher matcher = MESSAGE_PROPERTY_PATH_PATTERN.matcher(path);
        if (!matcher.matches())
            throw Exceptions.error("property path must follow 'path/name_language.properties' pattern, path={}", path);
        String languagePostfix = matcher.group(1);
        if (Strings.isEmpty(languagePostfix)) return DEFAULT_LANGUAGE;
        return languagePostfix.substring(1);
    }

    String effectiveLanguage(String language) {
        String currentLanguage = language;
        while (true) {
            if (messages.containsKey(currentLanguage)) return currentLanguage;
            int index = currentLanguage.lastIndexOf('_');
            if (index < 0) break;
            currentLanguage = currentLanguage.substring(0, index);
        }
        return DEFAULT_LANGUAGE;
    }

    MessageProvider messageProvider(String effectiveLanguage) {
        List<Properties> messageProperties = Lists.newArrayList();
        String currentLanguage = effectiveLanguage;
        while (true) {
            Properties properties = messages.get(currentLanguage);
            if (properties != null) messageProperties.add(properties);
            int index = currentLanguage.lastIndexOf('_');
            if (index < 0) break;
            currentLanguage = currentLanguage.substring(0, index);
        }
        Properties properties = messages.get(DEFAULT_LANGUAGE);
        if (properties != null) messageProperties.add(properties);
        return new MessageProviderImpl(messageProperties);
    }
}
