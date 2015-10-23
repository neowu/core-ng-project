package core.framework.impl.template.v2.html;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Files;
import core.framework.impl.template.v2.html.ast.Attribute;
import core.framework.impl.template.v2.html.ast.Comment;
import core.framework.impl.template.v2.html.ast.Document;
import core.framework.impl.template.v2.html.ast.Element;
import core.framework.impl.template.v2.html.ast.Node;
import core.framework.impl.template.v2.html.ast.Text;

import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author neo
 */
public class HTMLParser {
    private final HTMLLexer lexer;

    private Deque<Node> stack = new ArrayDeque<>();

    public HTMLParser(String html) {
        this.lexer = new HTMLLexer(html);
    }

    public static void main(String[] args) {
        String html = Files.text(Paths.get("etc/template.html"));
//        String html = "<html>\n" +
//            "  <body>\n" +
//            "    <p>\n" +
//            "      Hello World\n" +
//            "    </p>\n" +
//            "    <div> <img src=\"example.png\"/></div>\n" +
//            "  </body>\n" +
//            "</html>";
        Document parse = new HTMLParser(html).parse();
        System.out.println(parse.content());
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
            } else if (type == HTMLTokenType.TAG_START) {
                parseElement();
            } else if (type == HTMLTokenType.TAG_CLOSE) {
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
                throw Exceptions.error("can not find matched tag to close, tagName={}, L{}:{}", tagName, lexer.currentLine, lexer.currentColumn);
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
            if (currentType == HTMLTokenType.TAG_END_CLOSE) {
                currentElem.startTagClosed = true;
                return;
            } else if (currentType == HTMLTokenType.TAG_END) {
                stack.push(currentElem);

                if ("script".equals(currentElem.name) || "style".equals(currentElem.name)) {
                    lexer.nextScriptToken(currentElem.name);
                    addChild(new Text(lexer.currentToken()));
                }
                return;
            } else if (currentType == HTMLTokenType.EOF) {
                return;
            } else if (currentType == HTMLTokenType.ATTR_NAME) {
                currentAttribute = new Attribute(lexer.currentToken());
                currentElem.attributes.add(currentAttribute);
            } else if (currentType == HTMLTokenType.ATTR_VALUE) {
                if (currentAttribute == null)
                    throw Exceptions.error("attr is invalid, L{}:{}", lexer.currentLine, lexer.currentColumn);

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
