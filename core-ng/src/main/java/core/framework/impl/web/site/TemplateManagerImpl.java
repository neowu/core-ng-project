package core.framework.impl.web.site;

import core.framework.api.util.Files;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.web.Request;
import core.framework.api.web.site.TemplateManager;
import core.framework.impl.template.Template;
import core.framework.impl.template.TemplateBuilder;
import core.framework.impl.template.function.Function;
import core.framework.impl.template.source.FileTemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

/**
 * @author neo
 */
public class TemplateManagerImpl implements TemplateManager {
    private final Logger logger = LoggerFactory.getLogger(TemplateManagerImpl.class);
    private final Map<String, Template> templates = Maps.newConcurrentHashMap();
    private final WebDirectory webDirectory;
    private final MessageManager messageManager;
    private final CDNFunction cdnFunction = new CDNFunction();

    public TemplateManagerImpl(WebDirectory webDirectory, MessageManager messageManager) {
        this.webDirectory = webDirectory;
        this.messageManager = messageManager;
    }

    @Override
    public String process(String templatePath, Object model, Request request) {
        StopWatch watch = new StopWatch();
        try {
            Template template = templates.computeIfAbsent(templateKey(templatePath), (key) -> load(templatePath, model.getClass()));
            Map<String, Function> functions = functions(request);
            return template.process(model, functions);
        } finally {
            logger.debug("process, templatePath={}, elapsedTime={}", templatePath, watch.elapsedTime());
        }
    }

    private Map<String, Function> functions(Request request) {
        Map<String, Function> functions = Maps.newHashMap();
        functions.put("msg", new MessageFunction(messageManager, request));
        functions.put("cdn", cdnFunction);
        return functions;
    }

    public void add(String templatePath, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            templates.put(templateKey(templatePath), load(templatePath, modelClass));
        } finally {
            logger.info("add, templatePath={}, modelClass={}, elapsedTime={}", templatePath, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    public void cdnHosts(String... hosts) {
        logger.info("set cdn hosts, hosts={}", Arrays.toString(hosts));
        cdnFunction.hosts = hosts;
    }

    private Template load(String templatePath, Class<?> modelClass) {
        return new TemplateBuilder(new FileTemplateSource(webDirectory.root(), templatePath), modelClass).build();
    }

    private String templateKey(String templatePath) {
        if (webDirectory.localEnv) {
            Path path = webDirectory.path(templatePath);
            return templatePath + ":" + Files.lastModified(path).getEpochSecond();
        } else {
            return templatePath;
        }
    }
}
