package core.framework.impl.web.site;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Files;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.web.Request;
import core.framework.api.web.site.LanguageProvider;
import core.framework.api.web.site.TemplateManager;
import core.framework.api.web.site.WebDirectory;
import core.framework.impl.template.CDNManager;
import core.framework.impl.template.HTMLTemplate;
import core.framework.impl.template.HTMLTemplateBuilder;
import core.framework.impl.template.MessageManager;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.source.FileTemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class TemplateManagerImpl implements TemplateManager {
    public final MessageManager messageManager = new MessageManager();
    public final CDNManager cdnManager = new CDNManager();

    private final Logger logger = LoggerFactory.getLogger(TemplateManagerImpl.class);
    private final Map<String, HTMLTemplate> templates = Maps.newConcurrentHashMap();
    private final Map<String, Instant> templateLastModifiedTimes = Maps.newConcurrentHashMap();
    private final WebDirectory webDirectory;

    public LanguageProvider languageProvider;

    public TemplateManagerImpl(WebDirectory webDirectory) {
        this.webDirectory = webDirectory;
    }

    @Override
    public String process(String templatePath, Object model, Request request) {
        StopWatch watch = new StopWatch();
        try {
            HTMLTemplate template = get(templatePath, model.getClass(), request);
            TemplateContext stack = new TemplateContext(model);
            stack.cdn = cdnManager;
            return template.process(stack);
        } finally {
            logger.debug("process, templatePath={}, elapsedTime={}", templatePath, watch.elapsedTime());
        }
    }

    private HTMLTemplate get(String templatePath, Class<?> modelClass, Request request) {
        String templateKey = templateKey(templatePath, request);
        if (webDirectory.localEnv) {
            HTMLTemplate template = templates.get(templateKey);
            Path path = webDirectory.path(templatePath);
            if (template == null || Files.lastModified(path).isAfter(templateLastModifiedTimes.get(templatePath))) {
                templateLastModifiedTimes.put(templatePath, Files.lastModified(path)); // put modified time first, then template, for zero cost to handle local threading
                return load(templatePath, modelClass, templateKey);
            }
            return template;
        } else {
            HTMLTemplate template = templates.get(templateKey);
            if (template == null)
                return load(templatePath, modelClass, templateKey);
            return template;
        }
    }

    private String templateKey(String templatePath, Request request) {
        if (languageProvider == null) return templatePath;
        Optional<String> language = languageProvider.get(request);
        if (language.isPresent()) return templatePath + ":" + language.get();
        return templatePath;
    }

    public void add(String templatePath, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            load(templatePath, modelClass, null);
            if (webDirectory.localEnv) {
                Path path = webDirectory.path(templatePath);
                templateLastModifiedTimes.put(templatePath, Files.lastModified(path));
            }
        } finally {
            logger.info("add, templatePath={}, modelClass={}, elapsedTime={}", templatePath, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    private HTMLTemplate load(String templatePath, Class<?> modelClass, String templateKey) {
        HTMLTemplateBuilder builder = new HTMLTemplateBuilder(new FileTemplateSource(webDirectory.root(), templatePath), modelClass);
        builder.cdn = cdnManager;
        builder.message = messageManager;
        builder.parse();

        List<String> languages = messageManager.languages();
        if (languages.isEmpty()) {
            HTMLTemplate template = builder.build();
            templates.put(templatePath, template);
            return template;
        } else {
            HTMLTemplate matchedTemplate = null;
            for (String language : languages) {
                builder.language = language;
                HTMLTemplate template = builder.build();
                String key = MessageManager.DEFAULT_LANGUAGE_KEY.equals(language) ? templatePath : templatePath + ":" + language;
                if (key.equals(templateKey)) matchedTemplate = template;
                templates.put(key, template);
            }
            if (templateKey != null && matchedTemplate == null)
                throw Exceptions.error("can not find template, please check message/language, key={}", templateKey);
            return matchedTemplate;
        }
    }
}
