package core.framework.api.template;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.template.Template;
import core.framework.impl.template.TemplateBuilder;
import core.framework.impl.template.location.ClasspathTemplateLocation;
import core.framework.impl.template.location.StringTemplateLocation;
import core.framework.impl.template.location.TemplateLocation;
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

    public void addClasspathTemplate(String name, String templateClasspath, Class<?> modelClass) {
        add(name, new ClasspathTemplateLocation(templateClasspath), modelClass);
    }

    public void addStringTemplate(String name, String template, Class<?> modelClass) {
        add(name, new StringTemplateLocation(template), modelClass);
    }

    private void add(String name, TemplateLocation location, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            templates.put(name, new TemplateBuilder(location, modelClass).build());
        } finally {
            logger.info("add template, name={}, modelClass={}, elapsedTime={}", name, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }
}
