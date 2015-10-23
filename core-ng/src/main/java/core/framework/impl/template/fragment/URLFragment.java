package core.framework.impl.template.fragment;

import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

/**
 * @author neo
 */
public class URLFragment implements Fragment {
    private final ExpressionHolder expression;
    private final String value;
    private final boolean hasCDN;

    public URLFragment(String expression, CallTypeStack stack, String location, boolean hasCDN) {
        this.expression = new ExpressionBuilder(expression, stack, location).build();
        value = null;
        this.hasCDN = hasCDN;
    }

    public URLFragment(String value) {
        this.expression = null;
        this.value = value;
        this.hasCDN = true;
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        String url = value;
        if (expression != null) {
            url = String.valueOf(expression.eval(stack));
        }
        if (hasCDN) {
            builder.append(stack.cdn(url));
        } else {
            builder.append(url);
        }
    }
}
