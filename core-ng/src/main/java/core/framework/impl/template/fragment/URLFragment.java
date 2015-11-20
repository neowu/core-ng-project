package core.framework.impl.template.fragment;

import core.framework.api.util.URIBuilder;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class URLFragment implements Fragment {  // this is for dynamic href/src only, static href won't be processed during compilation
    static boolean isValidURL(String url) {
        int length = url.length();
        if (length == 0) return false;
        if (url.contains("javascript:")) return false;
        for (int i = 0; i < length; i++) {
            char ch = url.charAt(i);
            if (!URIBuilder.isValidURIChar(ch)) return false;
        }
        return true;
    }
    private final Logger logger = LoggerFactory.getLogger(URLFragment.class);
    private final ExpressionHolder expression;
    private final String location;
    private final boolean cdn;

    public URLFragment(String expression, TemplateMetaContext context, boolean cdn, String location) {
        this.expression = new ExpressionBuilder(expression, context, location).build();
        this.cdn = cdn;
        this.location = location;
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        String url = String.valueOf(expression.eval(context));
        builder.append(url(url, context));
    }

    private String url(String url, TemplateContext context) {
        if (!isValidURL(url)) {
            logger.warn("illegal url detected, url={}, location={}", url, location);
            return "";
        }
        return cdn ? context.cdn.url(url) : url;
    }
}
