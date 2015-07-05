package core.framework.impl.web.response;

/**
 * @author neo
 */
public class TextBody implements Body {
    final String text;

    public TextBody(String text) {
        this.text = text;
    }
}
