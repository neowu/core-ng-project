package core.framework.internal.template.fragment;

import core.framework.internal.reflect.GenericTypes;
import core.framework.internal.template.TemplateContext;
import core.framework.internal.template.TemplateMetaContext;
import core.framework.internal.template.expression.ExpressionBuilder;
import core.framework.internal.template.expression.ExpressionHolder;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class BooleanAttributeFragment implements Fragment {
    private final String name;
    private final ExpressionHolder expression;

    public BooleanAttributeFragment(String name, String expression, TemplateMetaContext context, String location) {
        this.name = name;
        this.expression = new ExpressionBuilder(expression, context, location).build();
        if (!Boolean.class.equals(GenericTypes.rawClass(this.expression.returnType)))
            throw new Error(format("boolean attribute expression must return Boolean, condition={}, returnType={}, location={}", expression, this.expression.returnType.getTypeName(), location));
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        Object result = expression.eval(context);
        if (Boolean.TRUE.equals(result)) {
            builder.append(' ').append(name);
        }
    }
}
