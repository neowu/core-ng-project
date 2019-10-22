package core.framework.internal.template.expression;

/**
 * @author neo
 */
class ValueToken implements Token {
    final String value;
    final Class<?> type;

    ValueToken(String value, Class<?> type) {
        this.value = value;
        this.type = type;
    }
}
