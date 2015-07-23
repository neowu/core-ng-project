package core.framework.impl.web.template;

import core.framework.impl.template.function.Function;
import core.framework.impl.web.RequestImpl;

/**
 * @author neo
 */
public class MessageFunction implements Function {
    private final RequestImpl request;

    public MessageFunction(RequestImpl request) {
        this.request = request;
    }

    @Override
    public Object apply(Object[] params) {
        String locale = request.cookie("locale").orElse(""); // todo: impl real one
        return params[0] + locale;
    }
}
