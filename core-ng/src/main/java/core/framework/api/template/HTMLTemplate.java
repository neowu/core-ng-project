package core.framework.api.template;

import core.framework.api.util.StopWatch;
import core.framework.impl.template.StringTemplateResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.StandardTemplateModeHandlers;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;
import java.util.Map;

/**
 * @author neo
 */
public final class HTMLTemplate {
    private final Logger logger = LoggerFactory.getLogger(HTMLTemplate.class);
    private final TemplateEngine templateEngine;
    private final StringTemplateResolver stringTemplateResolver;

    public HTMLTemplate() {
        StopWatch watch = new StopWatch();
        try {
            templateEngine = new TemplateEngine();
            stringTemplateResolver = new StringTemplateResolver();
            ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setTemplateMode(StandardTemplateModeHandlers.LEGACYHTML5.getTemplateModeName());
            templateEngine.addTemplateResolver(templateResolver);

            stringTemplateResolver.setTemplateMode(StandardTemplateModeHandlers.LEGACYHTML5.getTemplateModeName());
            templateEngine.addTemplateResolver(stringTemplateResolver);
        } finally {
            logger.info("create html template, elapsedTime={}", watch.elapsedTime());
        }
    }

    public void putTemplate(String templateName, String template) {
        logger.debug("put template, templateName={}", templateName);
        stringTemplateResolver.putTemplate(templateName, template);
    }

    public String process(String templateName, Map<String, Object> context) {
        logger.debug("process, templateName={}", templateName);
        return templateEngine.process(templateName, new Context(Locale.getDefault(), context));
    }
}
