package core.framework.impl.template.parser;

import core.framework.api.util.Exceptions;

/**
 * @author neo
 */
class HTMLLexer {
    private final String name;
    private final String html;

    private int startIndex;
    private int currentIndex;

    private int currentLine = 1;
    private int currentColumn = 1;

    public HTMLLexer(String name, String html) {
        this.name = name;
        this.html = html;
    }

    public HTMLTokenType nextNodeToken() {
        reset();

        if (currentIndex >= html.length()) {
            return HTMLTokenType.EOF;
        } else if (match(currentIndex, "<!--")) {
            move(4);
            return HTMLTokenType.START_COMMENT;
        } else if (match(currentIndex, "</")) {
            move(findEndTagLength());
            return HTMLTokenType.END_TAG;
        } else if (isStartTag(currentIndex)) {
            move(findStartTagLength());
            return HTMLTokenType.START_TAG;
        } else {
            move(findTextLength());
            return HTMLTokenType.TEXT;
        }
    }

    public HTMLTokenType nextElementToken() {
        skipWhitespaces();

        if (currentIndex >= html.length()) {
            return HTMLTokenType.EOF;
        } else if (match(currentIndex, ">")) {
            move(1);
            return HTMLTokenType.START_TAG_END;
        } else if (match(currentIndex, "/>")) {
            move(2);
            return HTMLTokenType.START_TAG_END_CLOSE;
        } else if (match(currentIndex, "=")) {
            move(1);
            skipWhitespaces();
            int length = findAttributeValueLength();
            if (length > 0) move(length);
            return HTMLTokenType.ATTRIBUTE_VALUE;
        } else {
            move(findAttributeNameLength());
            return HTMLTokenType.ATTRIBUTE_NAME;
        }
    }

    public HTMLTokenType nextEndCommentToken() {
        reset();

        int length = -1;
        for (int i = currentIndex; i < html.length() - 3; i++) {
            if (match(i, "-->")) {
                length = i - currentIndex;
                break;
            }
        }
        if (length == -1) throw Exceptions.error("comment is not closed, location={}", currentLocation());
        move(length);

        return HTMLTokenType.END_COMMENT;
    }

    public HTMLTokenType nextScriptToken(String tagName) {
        reset();
        String closeTag = "</" + tagName + ">";
        int length = -1;
        int maxIndex = html.length() - closeTag.length() + 1;
        for (int i = currentIndex; i < maxIndex; i++) {
            if (match(i, closeTag)) {
                length = i - currentIndex;
                break;
            }
        }
        if (length == -1) throw Exceptions.error("script/css is not closed, location={}", currentLocation());

        if (length > 0) {
            move(length);
            return HTMLTokenType.TEXT;
        } else {
            return HTMLTokenType.END_TAG;   // empty tag
        }
    }

    public String currentToken() {
        return html.substring(startIndex, currentIndex);
    }

    public String currentLocation() {
        return name + ":" + currentLine + ":" + currentColumn;
    }

    private void skipWhitespaces() {
        int length = 0;
        for (int i = currentIndex; i < html.length(); i++) {
            if (!Character.isWhitespace(html.charAt(i))) {
                break;
            }
            length++;
        }
        if (length > 0) move(length);
        reset();
    }

    private boolean isStartTag(int index) {
        if (index + 1 >= html.length()) return false;
        return html.charAt(index) == '<' && Character.isLetter(html.charAt(index + 1));
    }

    private int findStartTagLength() {
        for (int i = currentIndex + 1; i < html.length(); i++) {
            char ch = html.charAt(i);
            if (Character.isWhitespace(ch) || ch == '>' || ch == '/') {
                return i - currentIndex;
            }
        }
        throw Exceptions.error("start tag is invalid, location={}", currentLocation());
    }

    private int findEndTagLength() {
        for (int i = currentIndex + 2; i < html.length(); i++) {
            char ch = html.charAt(i);
            if (Character.isWhitespace(ch)) break;
            if (ch == '>') {
                return i - currentIndex + 1;
            }
        }
        throw Exceptions.error("end tag is invalid, location={}", currentLocation());
    }

    private int findTextLength() {
        int length = 0;
        for (int i = currentIndex; i < html.length(); i++) {
            if (isStartTag(i) || match(i, "<!--") || match(i, "</")) break;
            length++;
        }
        return length;
    }

    private int findAttributeNameLength() {
        for (int i = currentIndex; i < html.length(); i++) {
            char ch = html.charAt(i);
            if (Character.isWhitespace(ch) || ch == '=' || ch == '/' || ch == '>') {
                return i - currentIndex;
            }
            if (ch == '<') {
                throw Exceptions.error("attribute name is invalid, location={}", currentLocation());
            }
        }
        throw Exceptions.error("attribute name is invalid, location={}", currentLocation());
    }

    private int findAttributeValueLength() {
        boolean hasDoubleQuote = currentIndex < html.length() && html.charAt(currentIndex) == '"';
        int i = hasDoubleQuote ? currentIndex + 1 : currentIndex;
        for (; i < html.length(); i++) {
            char ch = html.charAt(i);
            if (!hasDoubleQuote && (Character.isWhitespace(ch) || ch == '>' || match(i, "/>"))) {
                return i - currentIndex;
            } else if (ch == '"') {
                return i - currentIndex + 1;
            }
        }
        throw Exceptions.error("attribute value is invalid, location={}", currentLocation());
    }

    private boolean match(int index, String token) {
        if (index + token.length() > html.length()) return false;
        for (int i = 0; i < token.length(); i++) {
            if (html.charAt(index + i) != token.charAt(i)) return false;
        }
        return true;
    }

    private void reset() {
        startIndex = currentIndex;
    }

    private void move(int length) {
        if (length == 0) throw Exceptions.error("syntax is invalid, location={}", currentLocation());
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
