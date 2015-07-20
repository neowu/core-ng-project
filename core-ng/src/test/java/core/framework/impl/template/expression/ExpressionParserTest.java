package core.framework.impl.template.expression;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class ExpressionParserTest {
    ExpressionParser parser = new ExpressionParser();

    @Test
    public void singleValue() {
        Token stringValue = parser.parse("\"text\"");
        assertThat(stringValue, instanceOf(ValueToken.class));
        Assert.assertEquals("\"text\"", ((ValueToken) stringValue).value);

        Token numberValue = parser.parse("12.00");
        assertThat(numberValue, instanceOf(ValueToken.class));
        Assert.assertEquals("12.00", ((ValueToken) numberValue).value);
    }

    @Test
    public void singleField() {
        Token token = parser.parse("field");
        assertThat(token, instanceOf(FieldToken.class));
        FieldToken fieldToken = (FieldToken) token;
        Assert.assertEquals("field", fieldToken.name);
        Assert.assertNull(fieldToken.next);
    }

    @Test
    public void singleMethod() {
        Token token = parser.parse("method()");
        assertThat(token, instanceOf(MethodToken.class));
        MethodToken methodToken = (MethodToken) token;
        Assert.assertEquals("method", methodToken.name);
        Assert.assertTrue(methodToken.params.isEmpty());
        Assert.assertNull(methodToken.next);
    }

    @Test
    public void builtinMethod() {
        Token token = parser.parse("#html(f1.f2.m1(f3.m2()))");

        MethodToken html = (MethodToken) token;
        Assert.assertEquals("#html", html.name);
        Assert.assertEquals(1, html.params.size());

        FieldToken f1 = (FieldToken) html.params.get(0);
        Assert.assertEquals("f1", f1.name);

        FieldToken f2 = (FieldToken) f1.next;
        Assert.assertEquals("f2", f2.name);

        MethodToken m1 = (MethodToken) f2.next;
        Assert.assertEquals("m1", m1.name);
        Assert.assertEquals(1, m1.params.size());
        FieldToken f3 = (FieldToken) m1.params.get(0);
        Assert.assertEquals("f3", f3.name);
        MethodToken m2 = (MethodToken) f3.next;
        Assert.assertEquals("m2", m2.name);
        Assert.assertTrue(m2.params.isEmpty());

        Assert.assertNull(m1.next);
    }

    @Test
    public void expression() {
        Token token = parser.parse("f1.f2.m1(f3.m2(), \"v1\", f4).f5");

        FieldToken f1 = (FieldToken) token;
        Assert.assertEquals("f1", f1.name);

        FieldToken f2 = (FieldToken) f1.next;
        Assert.assertEquals("f2", f2.name);

        MethodToken m1 = (MethodToken) f2.next;
        Assert.assertEquals("m1", m1.name);
        Assert.assertEquals(3, m1.params.size());
        FieldToken f3 = (FieldToken) m1.params.get(0);
        Assert.assertEquals("f3", f3.name);
        MethodToken m2 = (MethodToken) f3.next;
        Assert.assertEquals("m2", m2.name);
        Assert.assertTrue(m2.params.isEmpty());
        ValueToken v1 = (ValueToken) m1.params.get(1);
        Assert.assertEquals("\"v1\"", v1.value);
        FieldToken f4 = (FieldToken) m1.params.get(2);
        Assert.assertEquals("f4", f4.name);

        FieldToken f5 = (FieldToken) m1.next;
        Assert.assertEquals("f5", f5.name);
    }
}