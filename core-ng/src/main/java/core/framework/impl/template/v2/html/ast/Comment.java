package core.framework.impl.template.v2.html.ast;

/**
 * @author neo
 */
public class Comment implements Node {
    public final String content;

    public Comment(String content) {
        this.content = content;
    }

    @Override
    public void print(StringBuilder builder) {
        builder.append("<!--").append(content);
    }
}
