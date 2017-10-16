package core.framework.impl.template.expression;

import core.framework.impl.template.TemplateMetaContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ExpressionTranslatorTest {
    private ExpressionParser parser;

    @BeforeEach
    void createExpressionParser() {
        parser = new ExpressionParser();
    }

    @Test
    void text() {
        String expression = new ExpressionTranslator(parser.parse("\"text\""), new TemplateMetaContext(Object.class)).translate();
        assertEquals("\"text\"", expression);
    }

    @Test
    void field() {
        String expression = new ExpressionTranslator(parser.parse("field"), new TemplateMetaContext(Object.class)).translate();
        assertEquals("$root.field", expression);
    }

    @Test
    void method() {
        String expression = new ExpressionTranslator(parser.parse("method()"), new TemplateMetaContext(Object.class)).translate();
        assertEquals("$root.method()", expression);
    }

    @Test
    void contextVariable() {
        TemplateMetaContext stack = new TemplateMetaContext(Object.class);
        stack.paramClasses.put("item", Object.class);
        String expression = new ExpressionTranslator(parser.parse("item"), stack).translate();
        assertEquals("item", expression);
    }

    @Test
    void methodWithNumberParam() {
        String expression = new ExpressionTranslator(parser.parse("field.method(1)"), new TemplateMetaContext(Object.class)).translate();
        assertEquals("$root.field.method(1)", expression);
    }
}
