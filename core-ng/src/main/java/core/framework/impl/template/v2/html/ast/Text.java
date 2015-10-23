package core.framework.impl.template.v2.html.ast;

/**
 * @author neo
 */
public class Text implements Node {
    public final String content;

    public Text(String content) {
        this.content = content;
    }

    @Override
    public void print(StringBuilder builder) {
        builder.append(content);
    }
}
