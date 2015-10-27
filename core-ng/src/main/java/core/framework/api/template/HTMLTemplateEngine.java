package core.framework.api.template;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.template.HTMLTemplate;
import core.framework.impl.template.HTMLTemplateBuilder;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.source.StringTemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public final class HTMLTemplateEngine {
    private final Logger logger = LoggerFactory.getLogger(HTMLTemplateEngine.class);
    private final Map<String, HTMLTemplate> templates = Maps.newConcurrentHashMap();

    public String process(String name, Object model) {
        StopWatch watch = new StopWatch();
        try {
            HTMLTemplate template = templates.get(name);
            if (template == null) throw Exceptions.error("not found template, name={}", name);
            return template.process(new TemplateContext(model));
        } finally {
            logger.debug("process, name={}, elapsedTime={}", name, watch.elapsedTime());
        }
    }

    public void add(String name, String template, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            templates.put(name, new HTMLTemplateBuilder(new StringTemplateSource(name, template), modelClass).build());
        } finally {
            logger.info("add, name={}, modelClass={}, elapsedTime={}", name, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }
}
