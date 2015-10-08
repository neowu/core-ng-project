package core.framework.impl.template.expression;

/**
 * @author neo
 */
public class ValueToken implements Token {
    final String value;
    final Class<?> type;

    public ValueToken(String value, Class<?> type) {
        this.value = value;
        this.type = type;
    }
}
