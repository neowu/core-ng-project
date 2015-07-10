package core.framework.impl.template;

import java.util.Map;

/**
 * @author neo
 */
public class TextHandler implements TagHandler {
    private final String text;

    public TextHandler(String text) {
        this.text = text;
    }

    @Override
    public void process(StringBuilder builder, Map<String, Object> context) {
        builder.append(text);
    }
}
