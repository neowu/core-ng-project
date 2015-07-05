package core.framework.impl.template;

import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author neo
 */
public class StringTemplateResolver extends TemplateResolver {
    final Map<String, String> templates = Maps.newConcurrentHashMap();

    public StringTemplateResolver() {
        setResourceResolver(new StringResourceResolver());
    }

    @Override
    protected String computeResourceName(TemplateProcessingParameters params) {
        return params.getTemplateName();
    }

    public void putTemplate(String templateName, String template) {
        templates.put(templateName, template);
    }

    private class StringResourceResolver implements IResourceResolver {
        @Override
        public InputStream getResourceAsStream(TemplateProcessingParameters params, String resourceName) {
            String template = templates.get(resourceName);
            if (template == null) return null;
            return new ByteArrayInputStream(Strings.bytes(template));
        }

        @Override
        public String getName() {
            return StringResourceResolver.class.getSimpleName();
        }
    }
}
