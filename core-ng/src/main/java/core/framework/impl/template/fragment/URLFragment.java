package core.framework.impl.template.fragment;

import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class URLFragment implements Fragment {  // this is for dynamic href only, static href won't be touched during compilation
    private final Logger logger = LoggerFactory.getLogger(URLFragment.class);

    private final ExpressionHolder expression;
    private final String location;

    public URLFragment(String expression, TemplateMetaContext context, String location) {
        this.expression = new ExpressionBuilder(expression, context, location).build();
        this.location = location;
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        String url = String.valueOf(expression.eval(context));
        builder.append(sanitize(url));
    }

    private String sanitize(String url) {
        //TODO: better way to prevent XSS? href should be more strict than cdn
        if (url.contains("javascript:") || url.contains("<") || url.contains(">") || url.contains(" ")) {
            logger.warn("illegal url detected, url={}, location={}", url, location);
            return "";
        }
        return url;
    }
}
