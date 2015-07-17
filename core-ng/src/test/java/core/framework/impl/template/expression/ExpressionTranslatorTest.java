package core.framework.impl.template.expression;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class ExpressionTranslatorTest {
    @Test
    public void text() {
        String expression = new ExpressionTranslator("\"text\"", new CallTypeStack(Object.class)).translate();
        Assert.assertEquals("\"text\"", expression);
    }

    @Test
    public void field() {
        String expression = new ExpressionTranslator("field", new CallTypeStack(Object.class)).translate();
        Assert.assertEquals("root.field", expression);
    }

    @Test
    public void builtinMethod() {
        String expression = new ExpressionTranslator("#html(field)", new CallTypeStack(Object.class)).translate();
        Assert.assertEquals("stack.function(\"html\").apply(root.field)", expression);
    }

    @Test
    public void contextVariable() {
        CallTypeStack stack = new CallTypeStack(Object.class);
        stack.paramClasses.put("item", Object.class);
        String expression = new ExpressionTranslator("item", stack).translate();
        Assert.assertEquals("item", expression);
    }

    @Test
    public void methodCall() {
        CallTypeStack stack = new CallTypeStack(Object.class);
        stack.paramClasses.put("item", Object.class);
        String expression = new ExpressionTranslator("#html(field.method(), item.field, \"text\")", stack).translate();
        Assert.assertEquals("stack.function(\"html\").apply(root.field.method(),item.field,\"text\")", expression);
    }

    @Test
    public void number() {
        String expression = new ExpressionTranslator("field.method(1)", new CallTypeStack(Object.class)).translate();
        Assert.assertEquals("root.field.method(1)", expression);
    }
}