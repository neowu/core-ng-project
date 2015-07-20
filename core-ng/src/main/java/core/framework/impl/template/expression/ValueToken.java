package core.framework.impl.template.expression;

/**
 * @author neo
 */
public class ValueToken implements Token {
    final String value;

    public ValueToken(String value) {
        this.value = value;
    }
}
