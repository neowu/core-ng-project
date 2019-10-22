package core.framework.internal.template.fragment;

import core.framework.internal.template.TemplateContext;
import core.framework.internal.template.TemplateMetaContext;
import core.framework.internal.template.expression.ExpressionBuilder;
import core.framework.internal.template.expression.ExpressionHolder;

/**
 * @author neo
 */
public class HTMLContentFragment implements Fragment {
    private final ExpressionHolder expression;

    public HTMLContentFragment(String expression, TemplateMetaContext context, String location) {
        this.expression = new ExpressionBuilder(expression, context, location).build();
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        Object result = expression.eval(context);
        if (result != null) {
            builder.append(result);
        }
    }
}
