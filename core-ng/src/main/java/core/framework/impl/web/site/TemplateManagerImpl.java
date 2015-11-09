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

/**
 * @author neo
 */
public class TemplateManagerImpl implements TemplateManager {
    public final MessageManager messageManager = new MessageManager();
    public final CDNManager cdnManager = new CDNManager();

    private final Logger logger = LoggerFactory.getLogger(TemplateManagerImpl.class);
    private final Map<String, Templates> templates = Maps.newConcurrentHashMap();
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

    public void add(String templatePath, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            Templates previous = this.templates.putIfAbsent(templatePath, load(templatePath, modelClass));
            if (previous != null) throw Exceptions.error("template path is registered, templatePath={}", templatePath);
            if (webDirectory.localEnv) {
                Path path = webDirectory.path(templatePath);
                templateLastModifiedTimes.put(templatePath, Files.lastModified(path));
            }
        } finally {
            logger.info("add, templatePath={}, modelClass={}, elapsedTime={}", templatePath, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    private HTMLTemplate get(String templatePath, Class<?> modelClass, Request request) {
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

        return templates.get(language(request));
    }

    private String language(Request request) {
        return languageProvider == null ? null : languageProvider.get(request).orElse(null);
    }

    private Templates load(String templatePath, Class<?> modelClass) {
        HTMLTemplateBuilder builder = new HTMLTemplateBuilder(new FileTemplateSource(webDirectory.root(), templatePath), modelClass);
        builder.cdn = cdnManager;

        Templates templates = new Templates();
        List<String> languages = messageManager.languages();
        if (languages.isEmpty()) {
            templates.defaultTemplate = builder.build();
        } else {
            builder.message = messageManager;
            for (String language : languages) {
                builder.language = language;
                HTMLTemplate template = builder.build();
                templates.add(language, template);
            }
        }
        return templates;
    }
}
