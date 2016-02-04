package core.framework.impl.template.parser;

import core.framework.api.util.ASCII;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.impl.template.node.Attribute;
import core.framework.impl.template.node.Comment;
import core.framework.impl.template.node.ContainerNode;
import core.framework.impl.template.node.Document;
import core.framework.impl.template.node.Element;
import core.framework.impl.template.node.Text;
import core.framework.impl.template.source.TemplateSource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * @author neo
 */
public class HTMLParser {
    private final Set<String> voidElements = Sets.newHashSet("area", "base", "br", "col", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr");

    // loose checking to cover common cases, precise checking will be like e.g. checked attribute on input tag can be boolean attribute
    private final Set<String> booleanAttributes = Sets.newHashSet("checked", "selected", "disabled", "readonly", "multiple", "ismap", "defer");

    private final HTMLLexer lexer;
    private final Deque<ContainerNode> stack = new ArrayDeque<>();

    public HTMLParser(TemplateSource source) {
        this.lexer = new HTMLLexer(source.name(), source.content());
    }

    // only support subnet of HTML, which means enforce strict and consistence rules
    public Document parse() {
        Document document = new Document();
        stack.push(document);
        end:
        while (true) {
            HTMLTokenType type = lexer.nextNodeToken();
            switch (type) {
                case EOF:
                    break end;
                case TEXT:
                    stack.peek().add(new Text(lexer.currentToken()));
                    break;
                case START_COMMENT:
                    lexer.nextEndCommentToken();
                    stack.peek().add(new Comment(lexer.currentToken()));
                    break;
                case START_TAG:
                    String tagName = validateTagName(lexer.currentToken().substring(1));
                    parseElement(tagName);
                    break;
                case END_TAG:
                    String endTag = lexer.currentToken();
                    String endTagName = validateTagName(endTag.substring(2, endTag.length() - 1));
                    if (voidElements.contains(endTagName))
                        throw Exceptions.error("void element must not have close tag, tag={}, location={}", endTagName, lexer.currentLocation());
                    closeTag(endTagName);
                    break;
                default:
                    throw Exceptions.error("unexpected type, type={}, location={}", type, lexer.currentLocation());
            }
        }
        return document;
    }

    private void parseElement(String tagName) {
        Element currentElement = new Element(tagName);
        stack.peek().add(currentElement);

        Attribute currentAttribute = null;
        while (true) {
            HTMLTokenType type = lexer.nextElementToken();
            switch (type) {
                case EOF:
                    if (currentAttribute != null) validateAttribute(currentAttribute);
                    return;
                case START_TAG_END_CLOSE:
                    validateSelfCloseTag(tagName);
                    return;
                case START_TAG_END:
                    if (currentAttribute != null) validateAttribute(currentAttribute);
                    if (!voidElements.contains(tagName)) stack.push(currentElement);
                    if ("script".equals(currentElement.name) || "style".equals(currentElement.name)) {
                        HTMLTokenType contentType = lexer.nextScriptToken(currentElement.name);
                        if (contentType == HTMLTokenType.TEXT) stack.peek().add(new Text(lexer.currentToken()));
                    }
                    return;
                case ATTRIBUTE_NAME:
                    if (currentAttribute != null) validateAttribute(currentAttribute);
                    currentAttribute = new Attribute(lexer.currentToken(), tagName, lexer.currentLocation());
                    currentElement.attributes.add(currentAttribute);
                    break;
                case ATTRIBUTE_VALUE:
                    if (currentAttribute == null)
                        throw Exceptions.error("attribute syntax is invalid, location={}", lexer.currentLocation());
                    String attributeValue = lexer.currentToken();
                    if (attributeValue.startsWith("\"")) {
                        currentAttribute.value = attributeValue.substring(1, attributeValue.length() - 1);
                        currentAttribute.hasDoubleQuote = true;
                    } else if (!"".equals(attributeValue)) {    // not assign null attribute value, e.g. <p class=/>
                        currentAttribute.value = attributeValue;
                    }
                    break;
                default:
                    throw Exceptions.error("unexpected type, type={}, location={}", type, lexer.currentLocation());
            }
        }
    }

    private void closeTag(String tagName) {
        while (true) {
            ContainerNode lastNode = stack.pop();
            if (lastNode instanceof Document)
                throw Exceptions.error("can not find matched tag to close, tagName={}, location={}", tagName, lexer.currentLocation());
            Element element = (Element) lastNode;
            if (element.name.equals(tagName)) {
                element.hasEndTag = true;
                return;
            }
        }
    }

    private String validateTagName(String name) {
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (!ASCII.isLowerCase(ch) && !ASCII.isDigit(ch))
                throw Exceptions.error("tag name must only contain lower case letter or digit, name={}, location={}", name, lexer.currentLocation());
        }
        return name;
    }

    private void validateSelfCloseTag(String tagName) {
        if (voidElements.contains(tagName))
            throw Exceptions.error("we recommend not closing void element, tag={}, location={}", tagName, lexer.currentLocation());
        else
            throw Exceptions.error("non void element must not be self-closed, tag={}, location={}", tagName, lexer.currentLocation());
    }

    private void validateAttribute(Attribute attribute) {
        boolean isBooleanAttribute = booleanAttributes.contains(attribute.name);
        if (!isBooleanAttribute && attribute.value == null)
            throw Exceptions.error("non boolean attribute must have value, attribute={}>{}, location={}", attribute.tagName, attribute.name, attribute.location);
        if (isBooleanAttribute && attribute.value != null)
            throw Exceptions.error("we recommend not putting value for boolean attribute, attribute={}>{}, location={}", attribute.tagName, attribute.name, attribute.location);

        if (("link".equals(attribute.tagName) && "href".equals(attribute.name))
            || ("script".equals(attribute.tagName) && "src".equals(attribute.name))
            || ("img".equals(attribute.tagName) && "src".equals(attribute.name))) {
            validateStaticResourceURL(attribute);
        }
    }

    private void validateStaticResourceURL(Attribute attribute) {
        if (!attribute.value.startsWith("http://")
            && !attribute.value.startsWith("https://")
            && !attribute.value.startsWith("//")
            && !attribute.value.startsWith("/"))
            throw Exceptions.error("static resource url value must be either absolute or start with '/', attribute={}>{}, value={}, location={}",
                attribute.tagName, attribute.name, attribute.value, attribute.location);
    }
}
