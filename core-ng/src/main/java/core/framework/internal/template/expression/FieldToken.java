package core.framework.internal.template.expression;

/**
 * @author neo
 */
class FieldToken implements Token {
    final String name;
    Token next;

    FieldToken(String name) {
        this.name = name;
    }
}
