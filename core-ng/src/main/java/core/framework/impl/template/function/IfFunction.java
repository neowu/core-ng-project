package core.framework.impl.template.function;

import core.framework.api.util.Exceptions;

/**
 * @author neo
 */
public class IfFunction implements Function {
    @Override
    public Object apply(Object[] params) {
        if (params.length != 3) throw Exceptions.error("if function must have 3 params, params={}", params);
        if (!(params[0] instanceof Boolean))
            throw Exceptions.error("the first param of if function must be Boolean, param={}", params[0]);
        return Boolean.TRUE.equals(params[0]) ? params[1] : params[2];
    }
}
