package core.framework.impl.web.site;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Files;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.web.site.WebDirectory;
import core.framework.impl.template.CDNManager;
import core.framework.impl.template.HTMLTemplate;
import core.framework.impl.template.HTMLTemplateBuilder;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.source.FileTemplateSource;
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
    public final Map<String, Templates> templates = Maps.newConcurrentHashMap();
    private final MessageManager messageManager;
    private final Logger logger = LoggerFactory.getLogger(TemplateManager.class);
    private final Map<String, Instant> templateLastModifiedTimes = Maps.newConcurrentHashMap();
    private final WebDirectory webDirectory;

    public TemplateManager(WebDirectory webDirectory, MessageManager messageManager) {
        this.webDirectory = webDirectory;
        this.messageManager = messageManager;
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
            Templates previous = templates.putIfAbsent(templatePath, load(templatePath, modelClass));
            if (previous != null) throw Exceptions.error("template path is registered, templatePath={}", templatePath);
            if (webDirectory.localEnv) {
                Path path = webDirectory.path(templatePath);
                templateLastModifiedTimes.put(templatePath, Files.lastModified(path));
            }
        } finally {
            logger.info("add, templatePath={}, modelClass={}, elapsedTime={}", templatePath, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    private HTMLTemplate get(String templatePath, Class<?> modelClass, String language) {
        Templates templates = this.templates.get(templatePath);
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

        return templates.get(language == null ? MessageManager.DEFAULT_LANGUAGE : language);
    }

    private Templates load(String templatePath, Class<?> modelClass) {
        HTMLTemplateBuilder builder = new HTMLTemplateBuilder(new FileTemplateSource(webDirectory.root(), templatePath), modelClass);
        builder.cdn = cdnManager;
        Templates templates = new Templates();
        for (String language : messageManager.languages) {
            builder.message = key -> messageManager.get(key, language);
            HTMLTemplate htmlTemplate = builder.build();
            templates.add(language, htmlTemplate);
        }
        return templates;
    }
}
