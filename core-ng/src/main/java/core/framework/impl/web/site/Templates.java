package core.framework.impl.web.site;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.impl.template.HTMLTemplate;
import core.framework.impl.template.MessageManager;

import java.util.Map;

/**
 * @author neo
 */
public class Templates {
    public HTMLTemplate defaultTemplate;
    private Map<String, HTMLTemplate> languageTemplates;

    public void add(String language, HTMLTemplate template) {
        if (MessageManager.DEFAULT_LANGUAGE.equals(language)) {
            defaultTemplate = template;
        } else {
            if (languageTemplates == null) languageTemplates = Maps.newHashMap();
            languageTemplates.put(language, template);
        }
    }

    public HTMLTemplate get(String language) {
        if (language == null) {
            if (defaultTemplate == null) throw new Error("default template is not configured, please check messages");
            return defaultTemplate;
        }

        if (languageTemplates != null) {
            HTMLTemplate template = languageTemplates.get(language);
            if (template != null) return template;
        }

        throw Exceptions.error("template language is not defined, please check messages, language={}", language);
    }
}
