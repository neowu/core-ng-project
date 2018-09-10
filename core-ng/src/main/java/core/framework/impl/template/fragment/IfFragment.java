package core.framework.impl.template.fragment;

import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class IfFragment extends ContainerFragment {
    private static final Pattern STATEMENT_PATTERN = Pattern.compile("((!)?)([#a-zA-Z0-9\\.\\(\\)]+)");
    private final ExpressionHolder expression;
    private final boolean reverse;

    public IfFragment(String statement, TemplateMetaContext context, String location) {
        Matcher matcher = STATEMENT_PATTERN.matcher(statement);
        if (!matcher.matches())
            throw new Error(format("statement must match \"(!)condition\", statement={}, location={}", statement, location));

        reverse = "!".equals(matcher.group(2));
        String condition = matcher.group(3);

        expression = new ExpressionBuilder(condition, context, location).build();
        if (!Boolean.class.equals(GenericTypes.rawClass(expression.returnType)))
            throw new Error(format("if statement must return Boolean, condition={}, returnType={}, location={}", condition, expression.returnType.getTypeName(), location));
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        Object result = expression.eval(context);
        Boolean expected = reverse ? Boolean.FALSE : Boolean.TRUE;
        if (expected.equals(result)) {
            processChildren(builder, context);
        }
    }
}
