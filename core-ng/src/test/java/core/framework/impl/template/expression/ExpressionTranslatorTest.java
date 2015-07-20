package core.framework.impl.template.expression;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class ExpressionTranslatorTest {
    ExpressionParser parser = new ExpressionParser();
    ExpressionTranslator translator = new ExpressionTranslator();

    @Test
    public void text() {
        String expression = translator.translate(parser.parse("\"text\""), new CallTypeStack(Object.class));
        Assert.assertEquals("\"text\"", expression);
    }

    @Test
    public void field() {
        String expression = translator.translate(parser.parse("field"), new CallTypeStack(Object.class));
        Assert.assertEquals("$root.field", expression);
    }

    @Test
    public void builtinMethod() {
        String expression = translator.translate(parser.parse("#html(field)"), new CallTypeStack(Object.class));
        Assert.assertEquals("stack.function(\"html\").apply(new Object[]{$root.field})", expression);
    }

    @Test
    public void contextVariable() {
        CallTypeStack stack = new CallTypeStack(Object.class);
        stack.paramClasses.put("item", Object.class);
        String expression = translator.translate(parser.parse("item"), stack);
        Assert.assertEquals("item", expression);
    }

    @Test
    public void methodCall() {
        CallTypeStack stack = new CallTypeStack(Object.class);
        stack.paramClasses.put("item", Object.class);
        Token expression = parser.parse("#html(field.method(), item.field, \"text\")");
        Assert.assertEquals("stack.function(\"html\").apply(new Object[]{$root.field.method(),item.field,\"text\"})", translator.translate(expression, stack));
    }

    @Test
    public void methodWithNumberParam() {
        String expression = translator.translate(parser.parse("field.method(1)"), new CallTypeStack(Object.class));
        Assert.assertEquals("$root.field.method(1)", expression);
    }
}