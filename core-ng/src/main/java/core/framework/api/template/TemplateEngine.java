package core.framework.api.template;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.template.Template;
import core.framework.impl.template.TemplateBuilder;
import core.framework.impl.template.source.StringTemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public final class TemplateEngine {
    private final Logger logger = LoggerFactory.getLogger(TemplateEngine.class);
    private final Map<String, Template> templates = Maps.newConcurrentHashMap();

    public String process(String templateName, Object model) {
        StopWatch watch = new StopWatch();
        try {
            Template template = templates.get(templateName);
            if (template == null) throw Exceptions.error("not found template, name={}", templateName);
            return template.process(model, null);
        } finally {
            logger.debug("process, templateName={}, elapsedTime={}", templateName, watch.elapsedTime());
        }
    }

    public void addTemplate(String name, String templateContent, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            templates.put(name, new TemplateBuilder(new StringTemplateSource(name, templateContent), modelClass).build());
        } finally {
            logger.info("add template, name={}, modelClass={}, elapsedTime={}", name, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }
}
