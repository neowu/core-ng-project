package core.framework.impl.template.v2.html.ast;

/**
 * @author neo
 */
public class Attribute {
    public final String name;
    public String value;
    public boolean hasDoubleQuote;

    public Attribute(String name) {
        this.name = name;
    }

    public void print(StringBuilder builder) {
        builder.append(name);
        if (value != null) {
            builder.append("=");
            if (hasDoubleQuote) builder.append("\"");
            builder.append(value);
            if (hasDoubleQuote) builder.append("\"");
        }
    }
}
