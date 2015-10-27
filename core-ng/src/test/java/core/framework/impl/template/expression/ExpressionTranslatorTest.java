package core.framework.impl.template.expression;

import core.framework.impl.template.TemplateMetaContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class ExpressionTranslatorTest {
    ExpressionParser parser = new ExpressionParser();

    @Test
    public void text() {
        String expression = new ExpressionTranslator(parser.parse("\"text\""), new TemplateMetaContext(Object.class)).translate();
        Assert.assertEquals("\"text\"", expression);
    }

    @Test
    public void field() {
        String expression = new ExpressionTranslator(parser.parse("field"), new TemplateMetaContext(Object.class)).translate();
        Assert.assertEquals("$root.field", expression);
    }

    @Test
    public void method() {
        String expression = new ExpressionTranslator(parser.parse("method()"), new TemplateMetaContext(Object.class)).translate();
        Assert.assertEquals("$root.method()", expression);
    }

    @Test
    public void contextVariable() {
        TemplateMetaContext stack = new TemplateMetaContext(Object.class);
        stack.paramClasses.put("item", Object.class);
        String expression = new ExpressionTranslator(parser.parse("item"), stack).translate();
        Assert.assertEquals("item", expression);
    }

    @Test
    public void methodWithNumberParam() {
        String expression = new ExpressionTranslator(parser.parse("field.method(1)"), new TemplateMetaContext(Object.class)).translate();
        Assert.assertEquals("$root.field.method(1)", expression);
    }
}