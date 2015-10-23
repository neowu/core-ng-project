package core.framework.impl.template.expression;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class MethodToken implements Token {
    final String name;
    List<Token> params = Lists.newArrayList();
    Token next;

    public MethodToken(String name) {
        this.name = name;
    }
}
