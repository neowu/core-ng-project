package core.framework.impl.template.fragment;

import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class URLFragment implements Fragment {
    private final Logger logger = LoggerFactory.getLogger(URLFragment.class);

    private final ExpressionHolder expression;
    private final String value;
    private final String location;
    private final boolean hasCDN;

    public URLFragment(String expression, CallTypeStack stack, String location, boolean hasCDN) {
        this.expression = new ExpressionBuilder(expression, stack, location).build();
        this.location = location;
        value = null;
        this.hasCDN = hasCDN;
    }

    public URLFragment(String value) {
        this.expression = null;
        this.location = null;
        this.value = value;
        this.hasCDN = true;
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        String url = value;
        if (expression != null) {
            url = String.valueOf(expression.eval(stack));
            url = sanitize(url);
        }
        if (hasCDN) {
            builder.append(stack.cdn(url));
        } else {
            builder.append(url);
        }
    }

    private String sanitize(String url) {
        //TODO: better way to prevent XSS?
        if (url.contains("javascript:") || url.contains("<") || url.contains(">") || url.contains(" ")) {
            logger.warn("illegal url detected, url={}, location={}", url, location);
            return "";
        }
        return url;
    }
}
