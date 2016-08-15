package core.framework.impl.web.site;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.impl.template.HTMLTemplate;

import java.util.Map;

/**
 * @author neo
 */
class Templates {
    private final Map<String, HTMLTemplate> languageTemplates = Maps.newHashMap();

    public void add(String language, HTMLTemplate template) {
        languageTemplates.put(language, template);
    }

    public HTMLTemplate get(String language) {
        HTMLTemplate template = languageTemplates.get(language);
        if (template != null) return template;
        throw Exceptions.error("language is not defined, please check site().message(), language={}", language);
    }
}
