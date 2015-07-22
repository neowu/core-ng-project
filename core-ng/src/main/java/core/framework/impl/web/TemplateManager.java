package core.framework.impl.web;

import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.template.Template;
import core.framework.impl.template.TemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public String process(String templateName, Object model) {
        StopWatch watch = new StopWatch();
        try {
            Template template = templates.computeIfAbsent(templateKey(templateName), (key) -> loadTemplate(templateName, model.getClass()));
            //TODO: manage custom function
            return template.process(model, null);
        } finally {
            logger.debug("process, templateName={}, elapsedTime={}", templateName, watch.elapsedTime());
        }
    }

    public void addTemplate(String templateName, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            templates.put(templateKey(templateName), loadTemplate(templateName, modelClass));
        } finally {
            logger.info("add template, templateName={}, modelClass={}, elapsedTime={}", templateName, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    private Template loadTemplate(String templateName, Class<?> modelClass) {
        logger.debug("load template, path={}", templateName);
        String template = webDirectory.text(templateName);
        return new TemplateBuilder(template, modelClass).build();
    }

    private String templateKey(String templateName) {
        if (webDirectory.localEnv) {
            return templateName + ":" + webDirectory.lastModified(templateName).toMillis();
        } else {
            return templateName;
        }
    }
}
