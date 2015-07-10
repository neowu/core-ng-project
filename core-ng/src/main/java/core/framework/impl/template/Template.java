package core.framework.impl.template;

import java.util.Map;

/**
 * @author neo
 */
public class Template {
    private final TagHandler handler;

    public Template(TagHandler handler) {
        this.handler = handler;
    }

    public String process(Map<String, Object> context) {
        StringBuilder builder = new StringBuilder();
        handler.process(builder, context);
        return builder.toString();
    }
}
