package core.framework.impl.template;

/**
 * @author neo
 */
public class StaticHandler implements FragmentHandler {
    private final String content;

    public StaticHandler(String content) {
        this.content = content;
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        builder.append(content);
    }
}
