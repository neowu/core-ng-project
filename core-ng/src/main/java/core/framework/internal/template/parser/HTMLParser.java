package core.framework.internal.template.parser;

import core.framework.internal.template.node.Attribute;
import core.framework.internal.template.node.Comment;
import core.framework.internal.template.node.ContainerNode;
import core.framework.internal.template.node.Document;
import core.framework.internal.template.node.Element;
import core.framework.internal.template.node.Text;
import core.framework.internal.template.source.TemplateSource;
import core.framework.util.Strings;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.regex.Pattern;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class HTMLParser {
    private final Pattern tagNamePattern = Pattern.compile("[a-z]+[a-z0-9\\-]*");

    private final Set<String> voidElements = Set.of("area", "base", "br", "col", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr");

    // loose checking to cover common cases, precise checking will be like e.g. checked attribute on input tag can be boolean attribute
    private final Set<String> booleanAttributes = Set.of("checked", "selected", "disabled", "readonly", "multiple", "ismap", "defer", "required", "sortable", "autofocus", "allowfullscreen", "async", "hidden");

    private final HTMLLexer lexer;
    private final Deque<ContainerNode> nodes = new ArrayDeque<>();

    public HTMLParser(TemplateSource source) {
        this.lexer = new HTMLLexer(source.name(), source.content());
    }

    // only support subnet of HTML, which means enforce strict and consistence rules
    public Document parse() {
        Document document = new Document();
        nodes.push(document);
        end:
        while (true) {
            HTMLTokenType type = lexer.nextNodeToken();
            switch (type) {
                case EOF:
                    break end;
                case TEXT:
                    nodes.peek().add(new Text(lexer.currentToken()));
                    break;
                case START_COMMENT:
                    lexer.nextEndCommentToken();
                    nodes.peek().add(new Comment(lexer.currentToken()));
                    break;
                case START_TAG:
                    String tagName = validateTagName(lexer.currentToken().substring(1));
                    parseElement(tagName);
                    break;
                case END_TAG:
                    String endTag = lexer.currentToken();
                    String endTagName = validateTagName(endTag.substring(2, endTag.length() - 1));
                    if (voidElements.contains(endTagName))
                        throw new Error(format("void element must not have close tag, tag={}, location={}", endTagName, lexer.currentLocation()));
                    closeTag(endTagName);
                    break;
                default:
                    throw new Error(format("unexpected type, type={}, location={}", type, lexer.currentLocation()));
            }
        }
        return document;
    }

    private void parseElement(String tagName) {
        Element currentElement = new Element(tagName);
        nodes.peek().add(currentElement);

        Attribute currentAttribute = null;
        while (true) {
            HTMLTokenType type = lexer.nextElementToken();
            switch (type) {
                case EOF -> {
                    if (currentAttribute != null) validateAttribute(currentAttribute);
                    return;
                }
                case START_TAG_END_CLOSE -> {
                    validateSelfCloseTag(tagName);
                    return;
                }
                case START_TAG_END -> {
                    if (currentAttribute != null) validateAttribute(currentAttribute);
                    if (!voidElements.contains(tagName)) nodes.push(currentElement);
                    if ("script".equals(currentElement.name) || "style".equals(currentElement.name)) {
                        HTMLTokenType contentType = lexer.nextScriptToken(currentElement.name);
                        if (contentType == HTMLTokenType.TEXT) nodes.peek().add(new Text(lexer.currentToken()));
                    }
                    return;
                }
                case ATTRIBUTE_NAME -> {
                    if (currentAttribute != null) validateAttribute(currentAttribute);
                    currentAttribute = new Attribute(lexer.currentToken(), tagName, lexer.currentLocation());
                    currentElement.attributes.add(currentAttribute);
                }
                case ATTRIBUTE_VALUE -> {
                    if (currentAttribute == null)
                        throw new Error("attribute syntax is invalid, location=" + lexer.currentLocation());
                    String attributeValue = lexer.currentToken();
                    if (attributeValue.startsWith("\"")) {
                        currentAttribute.value = attributeValue.substring(1, attributeValue.length() - 1);
                        currentAttribute.hasDoubleQuote = true;
                    } else if (!"".equals(attributeValue)) {    // not assign null attribute value, e.g. <p class=/>
                        currentAttribute.value = attributeValue;
                    }
                }
                default -> throw new Error(format("unexpected type, type={}, location={}", type, lexer.currentLocation()));
            }
        }
    }

    private void closeTag(String tagName) {
        while (true) {
            ContainerNode lastNode = nodes.pop();
            if (lastNode instanceof Document)
                throw new Error(format("can not find matched tag to close, tagName={}, location={}", tagName, lexer.currentLocation()));
            Element element = (Element) lastNode;
            if (element.name.equals(tagName)) {
                element.hasEndTag = true;
                return;
            }
        }
    }

    private String validateTagName(String name) {
        if (!tagNamePattern.matcher(name).matches())
            throw new Error(format("tag name must match {}, name={}, location={}", tagNamePattern.pattern(), name, lexer.currentLocation()));
        return name;
    }

    private void validateSelfCloseTag(String tagName) {
        if (voidElements.contains(tagName))
            throw new Error(format("it is recommended not to close void element, tag={}, location={}", tagName, lexer.currentLocation()));
        else
            throw new Error(format("non void element must not be self-closed, tag={}, location={}", tagName, lexer.currentLocation()));
    }

    private void validateAttribute(Attribute attribute) {
        boolean isBooleanAttribute = booleanAttributes.contains(attribute.name);
        if (!isBooleanAttribute && attribute.value == null)
            throw new Error(format("non boolean attribute must have value, attribute={}>{}, location={}", attribute.tagName, attribute.name, attribute.location));
        if (isBooleanAttribute && attribute.value != null)
            throw new Error(format("it is recommended not to put value for boolean attribute, attribute={}>{}, location={}", attribute.tagName, attribute.name, attribute.location));

        if ("link".equals(attribute.tagName) && "href".equals(attribute.name)
            || "script".equals(attribute.tagName) && "src".equals(attribute.name)
            || "img".equals(attribute.tagName) && "src".equals(attribute.name)) {
            validateStaticURI(attribute);
        }
    }

    private void validateStaticURI(Attribute attribute) {
        String value = attribute.value;
        if (!value.startsWith("http://")
            && !value.startsWith("https://")
            && !value.startsWith("//")
            && !Strings.startsWith(value, '/')
            && !value.startsWith("data:"))
            throw new Error(format("static uri value must be either absolute or start with '/', attribute={}>{}, value={}, location={}", attribute.tagName, attribute.name, value, attribute.location));
    }
}
