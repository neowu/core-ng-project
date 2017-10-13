package core.framework.impl.template.expression;

import core.framework.util.Lists;

import java.util.List;

/**
 * @author neo
 */
class MethodToken implements Token {
    final String name;
    final List<Token> params = Lists.newArrayList();
    Token next;

    MethodToken(String name) {
        this.name = name;
    }
}
