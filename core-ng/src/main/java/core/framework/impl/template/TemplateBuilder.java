package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.stream.Collectors;

/**
 * @author neo
 */
public class TemplateBuilder {
    public Template build(String template) {
        Document document = Jsoup.parse(template);
        TagHandler handler = handler(document);
        return new Template(handler);
    }

    private TagHandler handler(Node node) {
        if (node instanceof Element) {
            Element element = ((Element) node);
            String tagName = element.tagName().toLowerCase();
            ElementHandler handler = new ElementHandler(tagName);
            element.attributes().asList().forEach(attribute -> handler.attributes.add(new Attribute(attribute.getKey(), attribute.getValue())));
            handler.children.addAll(element.childNodes().stream().map(this::handler).collect(Collectors.toList()));
            return handler;
        } else if (node instanceof TextNode) {
            return new TextHandler(((TextNode) node).text());
        } else if (node instanceof DocumentType) {
            ElementHandler handler = new ElementHandler("!doctype");
            handler.attributes.add(new Attribute("html", "true"));
            return handler;
        } else {
            throw Exceptions.error("unsupported node type, node={}", node);
        }
    }
}
