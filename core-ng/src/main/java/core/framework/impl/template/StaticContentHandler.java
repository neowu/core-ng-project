package core.framework.impl.template;

/**
 * @author neo
 */
public class StaticContentHandler implements FragmentHandler {
    private final String content;

    public StaticContentHandler(String content) {
        this.content = content;
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        builder.append(content);
    }
}
