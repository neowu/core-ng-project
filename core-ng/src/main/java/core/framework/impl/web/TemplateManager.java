package core.framework.impl.web;

import core.framework.api.util.Files;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.template.Template;
import core.framework.impl.template.TemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author neo
 */
public class TemplateManager {
    private final Logger logger = LoggerFactory.getLogger(TemplateManager.class);
    private final Map<String, Template> templates = Maps.newConcurrentHashMap();
    private final WebDirectory webDirectory;

    public TemplateManager(WebDirectory webDirectory) {
        this.webDirectory = webDirectory;
    }

    public String process(String templatePath, Object model) {
        StopWatch watch = new StopWatch();
        try {
            Template template = templates.computeIfAbsent(templateKey(templatePath), (key) -> load(templatePath, model.getClass()));
            //TODO: manage custom function
            return template.process(model, null);
        } finally {
            logger.debug("process, templatePath={}, elapsedTime={}", templatePath, watch.elapsedTime());
        }
    }

    public void add(String templatePath, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            templates.put(templateKey(templatePath), load(templatePath, modelClass));
        } finally {
            logger.info("add, templatePath={}, modelClass={}, elapsedTime={}", templatePath, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    private Template load(String templatePath, Class<?> modelClass) {
        logger.debug("load template, path={}", templatePath);
        String template = Files.text(webDirectory.path(templatePath));
        return new TemplateBuilder(template, modelClass).build();
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
