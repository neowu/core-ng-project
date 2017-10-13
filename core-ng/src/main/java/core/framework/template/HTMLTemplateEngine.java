package core.framework.template;

import core.framework.impl.template.CDNManager;
import core.framework.impl.template.HTMLTemplate;
import core.framework.impl.template.HTMLTemplateBuilder;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.source.ClasspathTemplateSource;
import core.framework.impl.template.source.StringTemplateSource;
import core.framework.impl.template.source.TemplateSource;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public final class HTMLTemplateEngine {
    private final Logger logger = LoggerFactory.getLogger(HTMLTemplateEngine.class);
    private final Map<String, HTMLTemplate> templates = Maps.newConcurrentHashMap();
    private final CDNManager cdnManager = new CDNManager();

    public String process(String name, Object model) {
        StopWatch watch = new StopWatch();
        try {
            HTMLTemplate template = templates.get(name);
            if (template == null) throw Exceptions.error("template not found, name={}", name);
            TemplateContext context = new TemplateContext(model, cdnManager);
            return template.process(context);
        } finally {
            logger.debug("process, name={}, elapsedTime={}", name, watch.elapsedTime());
        }
    }

    public void add(String name, String template, Class<?> modelClass) {
        add(new StringTemplateSource(name, template), modelClass);
    }

    public void add(String classpath, Class<?> modelClass) {
        add(new ClasspathTemplateSource(classpath), modelClass);
    }

    private void add(TemplateSource source, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        String name = source.name();
        try {
            HTMLTemplate previous = templates.putIfAbsent(name, new HTMLTemplateBuilder(source, modelClass).build());
            if (previous != null) throw Exceptions.error("template is already added, name={}", name);
        } finally {
            logger.info("add, name={}, modelClass={}, elapsedTime={}", name, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }
}
