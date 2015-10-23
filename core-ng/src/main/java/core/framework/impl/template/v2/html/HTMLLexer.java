package core.framework.impl.template.v2.html;

import core.framework.api.util.Exceptions;

/**
 * @author neo
 */
public class HTMLLexer {
    private final String html;

    int startIndex;
    int currentIndex;
    int currentLine = 1;
    int currentColumn = 1;

    public HTMLLexer(String html) {
        this.html = html;
    }

    public HTMLTokenType nextNodeToken() {
        reset();

        if (currentIndex >= html.length()) {
            return HTMLTokenType.EOF;
        }

        if (match(currentIndex, "<!--")) {
            move(4);
            return HTMLTokenType.COMMENT_START;
        }

        if (match(currentIndex, "</")) {
            move(findCloseElementLength());
            return HTMLTokenType.TAG_CLOSE;
        }

        if (isStartTag(currentIndex)) {
            move(findStartElementLength());
            return HTMLTokenType.TAG_START;
        }

        move(findTextLength());
        return HTMLTokenType.TEXT;
    }

    public HTMLTokenType nextElementToken() {
        // read all following white spaces
        int length = 0;
        for (int i = currentIndex; i < html.length(); i++) {
            if (!Character.isWhitespace(html.charAt(i))) {
                break;
            }
            length++;
        }
        if (length > 0) move(length);
        reset();

        if (currentIndex >= html.length()) {
            return HTMLTokenType.EOF;
        }

        if (match(currentIndex, ">")) {
            move(1);
            return HTMLTokenType.TAG_END;
        }

        if (match(currentIndex, "/>")) {
            move(2);
            return HTMLTokenType.TAG_END_CLOSE;
        }

        if (match(currentIndex, "=")) {
            move(findAttrValueLength());
            return HTMLTokenType.ATTR_VALUE;
        }

        move(findAttrNameLength());
        return HTMLTokenType.ATTR_NAME;
    }

    public HTMLTokenType nextCommentEndToken() {
        reset();

        int length = -1;
        for (int i = currentIndex; i < html.length() - 3; i++) {
            if (match(i, "-->")) {
                length = i - currentIndex;
                break;
            }
        }
        if (length == -1) throw Exceptions.error("comment is not closed, L{}:{}", currentLine, currentColumn);
        move(length);

        return HTMLTokenType.COMMENT_END;
    }

    public HTMLTokenType nextScriptToken(String tagName) {
        reset();
        String closeTag = "</" + tagName + ">";
        int length = -1;
        int maxLength = html.length() - closeTag.length();
        for (int i = currentIndex; i < maxLength; i++) {
            if (match(i, closeTag)) {
                length = i - currentIndex;
                break;
            }
        }
        if (length == -1) throw Exceptions.error("script/css is not closed, L{}:{}", currentLine, currentColumn);
        move(length);
        return HTMLTokenType.TEXT;
    }

    private int findStartElementLength() {
        for (int i = currentIndex + 1; i < html.length(); i++) {
            char ch = html.charAt(i);
            if (Character.isWhitespace(ch) || ch == '>' || ch == '/') {
                return i - currentIndex;
            }
        }
        throw Exceptions.error("start tag is invalid, L{}:{}", currentLine, currentColumn);
    }

    private int findCloseElementLength() {
        for (int i = currentIndex + 2; i < html.length(); i++) {
            char ch = html.charAt(i);
            if (Character.isWhitespace(ch)) break;
            if (ch == '>') {
                return i - currentIndex + 1;
            }
        }
        throw Exceptions.error("close tag is invalid, L{}:{}", currentLine, currentColumn);
    }

    private int findTextLength() {
        int length = 0;
        for (int i = currentIndex; i < html.length(); i++) {
            if (isStartTag(i) || match(i, "</")) break;
            length++;
        }
        return length;
    }

    private int findAttrNameLength() {
        for (int i = currentIndex; i < html.length(); i++) {
            char ch = html.charAt(i);
            if (Character.isWhitespace(ch) || ch == '=' || ch == '/' || ch == '>') return i - currentIndex;
        }
        throw Exceptions.error("attr is invalid, L{}:{}", currentLine, currentColumn);
    }

    private int findAttrValueLength() {
        boolean started = false;
        boolean hasDoubleQuote = false;
        for (int i = currentIndex + 1; i < html.length(); i++) {  // skip first '='
            char ch = html.charAt(i);
            if (!started && !Character.isWhitespace(ch)) {
                started = true;
                if (ch == '"') hasDoubleQuote = true;
            } else if (started) {
                if ((!hasDoubleQuote && (Character.isWhitespace(ch) || ch == '>'))) {
                    return i - currentIndex;
                } else if (ch == '"') {
                    return i - currentIndex + 1;
                }
            } else if (ch == '>' || ch == '/') {
                return i - currentIndex + 1;
            }
        }
        throw Exceptions.error("attr value is invalid, L{}:{}", currentLine, currentColumn);
    }

    public String currentToken() {
        return html.substring(startIndex, currentIndex);
    }

    private boolean isStartTag(int index) {
        if (index + 1 >= html.length()) return false;
        return html.charAt(index) == '<' && Character.isLetter(html.charAt(index + 1));
    }

    private boolean match(int index, String token) {
        if (index + token.length() >= html.length()) return false;
        for (int i = 0; i < token.length(); i++) {
            if (html.charAt(index + i) != token.charAt(i)) return false;
        }
        return true;
    }

    private void reset() {
        startIndex = currentIndex;
    }

    private void move(int length) {
        if (length == 0) throw Exceptions.error("syntax is invalid, L{}:{}", currentLine, currentColumn);
        for (int i = 0; i < length; i++) {
            char ch = html.charAt(currentIndex);
            if (ch == '\n') {
                currentLine++;
                currentColumn = 1;
            } else {
                currentColumn++;
            }
            currentIndex++;
        }
    }
}
