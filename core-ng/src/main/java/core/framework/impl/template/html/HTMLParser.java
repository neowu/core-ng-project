package core.framework.impl.template.html;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.html.node.Attribute;
import core.framework.impl.template.html.node.Comment;
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

    private Deque<Node> stack = new ArrayDeque<>();

    public HTMLParser(TemplateSource source) {
        this.lexer = new HTMLLexer(source.source(), source.content());
    }

    public Document parse() {
        Document document = new Document();
        stack.push(document);
        while (true) {
            HTMLTokenType type = lexer.nextNodeToken();
            if (type == HTMLTokenType.TEXT) {
                addChild(new Text(lexer.currentToken()));
            } else if (type == HTMLTokenType.COMMENT_START) {
                lexer.nextCommentEndToken();
                addChild(new Comment(lexer.currentToken()));
            } else if (type == HTMLTokenType.START_TAG) {
                parseElement();
            } else if (type == HTMLTokenType.END_TAG) {
                String closeTag = lexer.currentToken();
                String tagName = closeTag.substring(2, closeTag.length() - 1);
                closeTag(tagName);
            } else if (type == HTMLTokenType.EOF) {
                break;
            }
        }
        return document;
    }

    private void closeTag(String tagName) {
        while (true) {
            Node lastNode = stack.pop();
            if (lastNode instanceof Document)
                throw Exceptions.error("can not find matched tag to close, tagName={}, location={}", tagName, lexer.currentLocation());
            Element element = (Element) lastNode;
            if (element.name.equals(tagName)) {
                element.hasCloseTag = true;
                return;
            }
        }
    }

    private void parseElement() {
        Element currentElem = new Element(lexer.currentToken().substring(1));
        addChild(currentElem);

        Attribute currentAttribute = null;
        while (true) {
            HTMLTokenType currentType = lexer.nextElementToken();
            if (currentType == HTMLTokenType.START_TAG_END_CLOSE) {
                currentElem.startTagClosed = true;
                return;
            } else if (currentType == HTMLTokenType.START_TAG_END) {
                stack.push(currentElem);

                if ("script".equals(currentElem.name) || "style".equals(currentElem.name)) {
                    lexer.nextScriptToken(currentElem.name);
                    addChild(new Text(lexer.currentToken()));
                }
                return;
            } else if (currentType == HTMLTokenType.EOF) {
                return;
            } else if (currentType == HTMLTokenType.ATTR_NAME) {
                String attrName = lexer.currentToken();
                currentAttribute = new Attribute(attrName);
                if (attrName.startsWith("c:")) currentAttribute.location = lexer.currentLocation();
                currentElem.attributes.add(currentAttribute);
            } else if (currentType == HTMLTokenType.ATTR_VALUE) {
                if (currentAttribute == null)
                    throw Exceptions.error("attr is invalid, location={}", lexer.currentLocation());

                String attrValue = lexer.currentToken();
                if (attrValue.startsWith("=\"")) {
                    currentAttribute.value = attrValue.substring(2, attrValue.length() - 1);
                    currentAttribute.hasDoubleQuote = true;
                } else
                    currentAttribute.value = attrValue.substring(1);
            }
        }
    }

    private void addChild(Node node) {
        Node currentNode = stack.peek();
        if (currentNode instanceof Document) {
            ((Document) currentNode).nodes.add(node);
        } else {
            ((Element) currentNode).nodes.add(node);
        }
    }
}
