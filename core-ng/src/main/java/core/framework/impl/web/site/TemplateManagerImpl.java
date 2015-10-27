package core.framework.impl.web.site;

import core.framework.api.util.Files;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.web.Request;
import core.framework.api.web.site.TemplateManager;
import core.framework.api.web.site.WebDirectory;
import core.framework.impl.template.HTMLTemplate;
import core.framework.impl.template.HTMLTemplateBuilder;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.source.FileTemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

/**
 * @author neo
 */
public class TemplateManagerImpl implements TemplateManager {
    private final Logger logger = LoggerFactory.getLogger(TemplateManagerImpl.class);
    private final Map<String, HTMLTemplate> templates = Maps.newConcurrentHashMap();
    private final Map<String, Instant> templateLastModifiedTimes = Maps.newConcurrentHashMap();
    private final WebDirectory webDirectory;
    private final MessageManager messageManager;
    private final CDNManager cdnManager = new CDNManager();

    public TemplateManagerImpl(WebDirectory webDirectory, MessageManager messageManager) {
        this.webDirectory = webDirectory;
        this.messageManager = messageManager;
    }

    @Override
    public String process(String templatePath, Object model, Request request) {
        StopWatch watch = new StopWatch();
        try {
            HTMLTemplate template = get(templatePath, model.getClass());
            TemplateContext stack = new TemplateContext(model);
            stack.cdn = cdnManager;
            stack.messageFunction = new MessageFunctionImpl(messageManager, request);
            return template.process(stack);
        } finally {
            logger.debug("process, templatePath={}, elapsedTime={}", templatePath, watch.elapsedTime());
        }
    }

    private HTMLTemplate get(String templatePath, Class<?> modelClass) {
        if (webDirectory.localEnv) {
            HTMLTemplate template = templates.get(templatePath);
            Path path = webDirectory.path(templatePath);
            if (template == null || Files.lastModified(path).isAfter(templateLastModifiedTimes.get(templatePath))) {
                template = load(templatePath, modelClass);
                templateLastModifiedTimes.put(templatePath, Files.lastModified(path)); // put modified time first, then template, for zero cost to handle local threading
                templates.putIfAbsent(templatePath, template);
            }
            return template;
        } else {
            return templates.computeIfAbsent(templatePath, (key) -> load(templatePath, modelClass));
        }
    }

    public void add(String templatePath, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            templates.put(templatePath, load(templatePath, modelClass));
            if (webDirectory.localEnv) {
                Path path = webDirectory.path(templatePath);
                templateLastModifiedTimes.put(templatePath, Files.lastModified(path));
            }
        } finally {
            logger.info("add, templatePath={}, modelClass={}, elapsedTime={}", templatePath, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    public void cdnHosts(String... hosts) {
        logger.info("set cdn hosts, hosts={}", Arrays.toString(hosts));
        cdnManager.hosts = hosts;
    }

    public void cdnVersion(String version) {
        logger.info("set cdn version, version={}", version);
        cdnManager.version = version;
    }

    private HTMLTemplate load(String templatePath, Class<?> modelClass) {
        HTMLTemplateBuilder builder = new HTMLTemplateBuilder(new FileTemplateSource(webDirectory.root(), templatePath), modelClass);
        if (cdnManager.hosts != null) builder.cdn(cdnManager);
        return builder.build();
    }
}
