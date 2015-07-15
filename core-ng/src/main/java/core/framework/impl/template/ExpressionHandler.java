package core.framework.impl.template;

/**
 * @author neo
 */
public class ExpressionHandler implements FragmentHandler {
    private final String expression;

    public ExpressionHandler(String expression) {
        this.expression = expression;
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        builder.append(context.eval(expression));
    }
}
