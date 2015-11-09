package core.framework.impl.template.parser;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.api.util.Strings;
import core.framework.impl.template.node.Attribute;
import core.framework.impl.template.node.Comment;
import core.framework.impl.template.node.ContainerNode;
import core.framework.impl.template.node.Document;
import core.framework.impl.template.node.Element;
import core.framework.impl.template.node.Node;
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
                    addChild(new Text(lexer.currentToken()));
                    break;
                case START_COMMENT:
                    lexer.nextEndCommentToken();
                    addChild(new Comment(lexer.currentToken()));
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
        addChild(currentElement);

        Attribute currentAttribute = null;
        while (true) {
            HTMLTokenType type = lexer.nextElementToken();
            switch (type) {
                case EOF:
                    return;
                case START_TAG_END_CLOSE:
                    if (voidElements.contains(tagName))
                        throw Exceptions.error("we recommend not closing void element, tag={}, location={}", tagName, lexer.currentLocation());
                    else
                        throw Exceptions.error("non void element must not be self-closed, tag={}, location={}", tagName, lexer.currentLocation());
                case START_TAG_END:
                    if (!voidElements.contains(tagName)) stack.push(currentElement);
                    if ("script".equals(currentElement.name) || "style".equals(currentElement.name)) {
                        HTMLTokenType contentType = lexer.nextScriptToken(currentElement.name);
                        if (contentType == HTMLTokenType.TEXT) addChild(new Text(lexer.currentToken()));
                    }
                    return;
                case ATTR_NAME:
                    currentAttribute = new Attribute(lexer.currentToken());
                    if (currentAttribute.isDynamic()) currentAttribute.location = lexer.currentLocation();
                    currentElement.attributes.add(currentAttribute);
                    break;
                case ATTR_VALUE:
                    if (currentAttribute == null)
                        throw Exceptions.error("attr is invalid, location={}", lexer.currentLocation());

                    if (booleanAttributes.contains(currentAttribute.name))
                        throw Exceptions.error("we recommend not putting value for boolean attribute, attribute={}, location={}", currentAttribute.name, lexer.currentLocation());

                    String attrValue = lexer.currentToken();
                    if (attrValue.startsWith("=\"")) {
                        currentAttribute.value = attrValue.substring(2, attrValue.length() - 1);
                        currentAttribute.hasDoubleQuote = true;
                    } else
                        currentAttribute.value = attrValue.substring(1);
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
            if (!(Strings.isLowerCase(ch) || (ch >= '0' && ch <= '9')))
                throw Exceptions.error("tag name must only contain lower case letter or digit, name={}, location={}", name, lexer.currentLocation());
        }
        return name;
    }

    private void addChild(Node node) {
        ContainerNode currentNode = stack.peek();
        currentNode.add(node);
    }
}
