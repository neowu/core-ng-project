package core.framework.impl.template.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ExpressionParserTest {
    private ExpressionParser parser;

    @BeforeEach
    void createExpressionParser() {
        parser = new ExpressionParser();
    }

    @Test
    void singleValue() {
        Token stringValue = parser.parse("\"text\"");
        assertThat(stringValue).isInstanceOf(ValueToken.class);
        assertEquals("\"text\"", ((ValueToken) stringValue).value);

        Token numberValue = parser.parse("12.00");
        assertThat(numberValue).isInstanceOf(ValueToken.class);
        assertEquals("12.00", ((ValueToken) numberValue).value);
    }

    @Test
    void singleQuoteString() {
        Token stringValue = parser.parse("'text'");
        assertThat(stringValue).isInstanceOf(ValueToken.class);
        assertEquals("\"text\"", ((ValueToken) stringValue).value);
    }

    @Test
    void singleField() {
        Token token = parser.parse("field");
        assertThat(token).isInstanceOf(FieldToken.class);
        FieldToken fieldToken = (FieldToken) token;
        assertEquals("field", fieldToken.name);
        assertNull(fieldToken.next);
    }

    @Test
    void singleMethod() {
        Token token = parser.parse("method()");
        assertThat(token).isInstanceOf(MethodToken.class);
        MethodToken methodToken = (MethodToken) token;
        assertEquals("method", methodToken.name);
        assertTrue(methodToken.params.isEmpty());
        assertNull(methodToken.next);
    }

    @Test
    void expression() {
        Token token = parser.parse("f1.f2.m1(f3.m2(), \"v1\", f4).f5");

        FieldToken f1 = (FieldToken) token;
        assertEquals("f1", f1.name);

        FieldToken f2 = (FieldToken) f1.next;
        assertEquals("f2", f2.name);

        MethodToken m1 = (MethodToken) f2.next;
        assertEquals("m1", m1.name);
        assertEquals(3, m1.params.size());
        FieldToken f3 = (FieldToken) m1.params.get(0);
        assertEquals("f3", f3.name);
        MethodToken m2 = (MethodToken) f3.next;
        assertEquals("m2", m2.name);
        assertTrue(m2.params.isEmpty());
        ValueToken v1 = (ValueToken) m1.params.get(1);
        assertEquals("\"v1\"", v1.value);
        FieldToken f4 = (FieldToken) m1.params.get(2);
        assertEquals("f4", f4.name);

        FieldToken f5 = (FieldToken) m1.next;
        assertEquals("f5", f5.name);
    }
}
