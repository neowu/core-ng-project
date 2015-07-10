package core.framework.impl.template;

import core.framework.api.util.Lists;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ElementHandler implements TagHandler {
    public final String tagName;
    public List<Attribute> attributes = Lists.newArrayList();
    public final List<TagHandler> children = Lists.newArrayList();

    public ElementHandler(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public void process(StringBuilder builder, Map<String, Object> context) {
        builder.append('<').append(tagName);
        for (Attribute attribute : attributes) {
            builder.append(' ').append(attribute.name).append("=\"").append(attribute.value).append("\"");
        }
        builder.append(">\n");

        for (TagHandler child : children) {
            child.process(builder, context);
        }
        builder.append("</").append(tagName).append('>');
    }
}
