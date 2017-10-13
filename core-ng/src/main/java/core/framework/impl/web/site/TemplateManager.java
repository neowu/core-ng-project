package core.framework.impl.web.site;

import core.framework.impl.template.CDNManager;
import core.framework.impl.template.HTMLTemplate;
import core.framework.impl.template.HTMLTemplateBuilder;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.source.FileTemplateSource;
import core.framework.util.Exceptions;
import core.framework.util.Files;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import core.framework.web.site.WebDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class TemplateManager {
    public final CDNManager cdnManager = new CDNManager();
    public final Map<String, Map<String, HTMLTemplate>> templates = Maps.newConcurrentHashMap();    // path->language->template
    private final MessageImpl message;
    private final Logger logger = LoggerFactory.getLogger(TemplateManager.class);
    private final Map<String, Instant> templateLastModifiedTimes = Maps.newConcurrentHashMap();
    private final WebDirectory webDirectory;

    public TemplateManager(WebDirectory webDirectory, MessageImpl message) {
        this.webDirectory = webDirectory;
        this.message = message;
    }

    public String process(String templatePath, Object model, String language) {
        StopWatch watch = new StopWatch();
        try {
            HTMLTemplate template = get(templatePath, model.getClass(), language);
            TemplateContext context = new TemplateContext(model, cdnManager);
            return template.process(context);
        } finally {
            logger.debug("process, templatePath={}, elapsedTime={}", templatePath, watch.elapsedTime());
        }
    }

    public void add(String templatePath, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            Map<String, HTMLTemplate> previous = templates.putIfAbsent(templatePath, load(templatePath, modelClass));
            if (previous != null) throw Exceptions.error("template was registered, templatePath={}", templatePath);
            if (webDirectory.localEnv) {
                Path path = webDirectory.path(templatePath);
                templateLastModifiedTimes.put(templatePath, Files.lastModified(path));
            }
        } finally {
            logger.info("add, templatePath={}, modelClass={}, elapsedTime={}", templatePath, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    private HTMLTemplate get(String templatePath, Class<?> modelClass, String language) {
        Map<String, HTMLTemplate> templates = this.templates.get(templatePath);
        if (templates == null)
            throw Exceptions.error("template is not registered, please use site().template() to add template, templatePath={}", templatePath);

        if (webDirectory.localEnv) {
            Path path = webDirectory.path(templatePath);
            if (Files.lastModified(path).isAfter(templateLastModifiedTimes.get(templatePath))) {
                templateLastModifiedTimes.put(templatePath, Files.lastModified(path)); // put modified time first, then template, for zero cost to handle local threading
                templates = load(templatePath, modelClass);
                this.templates.put(templatePath, templates);
            }
        }

        String targetLanguage = language == null ? MessageImpl.DEFAULT_LANGUAGE : language;
        HTMLTemplate template = templates.get(targetLanguage);
        if (template == null) throw Exceptions.error("language is not defined, please check site().message(), language={}", targetLanguage);
        return template;
    }

    private Map<String, HTMLTemplate> load(String templatePath, Class<?> modelClass) {
        HTMLTemplateBuilder builder = new HTMLTemplateBuilder(new FileTemplateSource(webDirectory.root(), templatePath), modelClass);
        builder.cdn = cdnManager;
        Map<String, HTMLTemplate> templates = Maps.newHashMap();
        for (String language : message.languages) {
            builder.message = key -> message.get(key, language);
            HTMLTemplate htmlTemplate = builder.build();
            templates.put(language, htmlTemplate);
        }
        return templates;
    }
}
