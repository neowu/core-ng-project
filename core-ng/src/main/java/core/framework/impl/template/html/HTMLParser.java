package core.framework.impl.template.html;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.impl.template.html.node.Attribute;
import core.framework.impl.template.html.node.Comment;
import core.framework.impl.template.html.node.ContainerNode;
import core.framework.impl.template.html.node.Document;
import core.framework.impl.template.html.node.Element;
import core.framework.impl.template.html.node.Node;
import core.framework.impl.template.html.node.Text;
import core.framework.impl.template.source.TemplateSource;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author neo
 */
public class HTMLParser {
    private final HTMLLexer lexer;

    private final Deque<ContainerNode> stack = new ArrayDeque<>();

    public HTMLParser(TemplateSource source) {
        this.lexer = new HTMLLexer(source.source(), source.content());
    }

    public Document parse() {
        Document document = new Document();
        stack.push(document);
        while (true) {
            HTMLTokenType type = lexer.nextNodeToken();
            if (type == HTMLTokenType.EOF) {
                break;
            } else if (type == HTMLTokenType.TEXT) {
                addChild(new Text(lexer.currentToken()));
            } else if (type == HTMLTokenType.START_COMMENT) {
                lexer.nextEndCommentToken();
                addChild(new Comment(lexer.currentToken()));
            } else if (type == HTMLTokenType.START_TAG) {
                String tagName = lexer.currentToken().substring(1);
                validateTagName(tagName);
                parseElement(tagName);
            } else if (type == HTMLTokenType.END_TAG) {
                String endTag = lexer.currentToken();
                String tagName = endTag.substring(2, endTag.length() - 1);
                validateTagName(tagName);
                closeTag(tagName);
            } else {
                throw Exceptions.error("unexpected type, type={}, location={}", type, lexer.currentLocation());
            }
        }
        return document;
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

    private void parseElement(String tagName) {
        Element currentElem = new Element(tagName);
        addChild(currentElem);

        Attribute currentAttribute = null;
        while (true) {
            HTMLTokenType type = lexer.nextElementToken();
            if (type == HTMLTokenType.EOF) {
                return;
            } else if (type == HTMLTokenType.START_TAG_END_CLOSE) {
                currentElem.startTagClosed = true;
                return;
            } else if (type == HTMLTokenType.START_TAG_END) {
                stack.push(currentElem);
                if ("script".equals(currentElem.name) || "style".equals(currentElem.name)) {
                    lexer.nextScriptToken(currentElem.name);
                    addChild(new Text(lexer.currentToken()));
                }
                return;
            } else if (type == HTMLTokenType.ATTR_NAME) {
                String attrName = lexer.currentToken();
                currentAttribute = new Attribute(attrName);
                if (attrName.startsWith("c:")) currentAttribute.location = lexer.currentLocation();
                currentElem.attributes.add(currentAttribute);
            } else if (type == HTMLTokenType.ATTR_VALUE) {
                if (currentAttribute == null)
                    throw Exceptions.error("attr is invalid, location={}", lexer.currentLocation());

                String attrValue = lexer.currentToken();
                if (attrValue.startsWith("=\"")) {
                    currentAttribute.value = attrValue.substring(2, attrValue.length() - 1);
                    currentAttribute.hasDoubleQuote = true;
                } else
                    currentAttribute.value = attrValue.substring(1);
            } else {
                throw Exceptions.error("unexpected type, type={}, location={}", type, lexer.currentLocation());
            }
        }
    }

    private void validateTagName(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (Strings.isUpperCase(name.charAt(i)))
                throw Exceptions.error("tag name must be in lower case, name={}, location={}", name, lexer.currentLocation());
        }
    }

    private void addChild(Node node) {
        ContainerNode currentNode = stack.peek();
        currentNode.add(node);
    }
}
