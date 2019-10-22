package core.framework.internal.template.parser;

import core.framework.internal.template.node.Document;
import core.framework.internal.template.node.Element;
import core.framework.internal.template.node.Text;
import core.framework.internal.template.source.StringTemplateSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class HTMLParserTest {
    @Test
    void voidElements() {
        String content = "<html><div><img src=//img.png>text</div></html>";

        Document document = new HTMLParser(new StringTemplateSource("test", content)).parse();
        assertEquals(1, document.nodes.size());

        Element html = (Element) document.nodes.get(0);
        assertEquals("html", html.name);
        assertEquals(1, html.nodes.size());

        Element div = (Element) html.nodes.get(0);
        assertEquals("div", div.name);
        assertEquals(2, div.nodes.size());
        assertTrue(div.hasEndTag);

        Element img = (Element) div.nodes.get(0);
        assertEquals("img", img.name);
        assertFalse(img.hasEndTag);
        assertEquals("//img.png", img.attributes.attributes.get("src").value);

        Text text = (Text) div.nodes.get(1);
        assertEquals("text", text.content);
    }

    @Test
    void emptyScript() {
        String content = "<script type=\"text/javascript\"></script>";

        Document document = new HTMLParser(new StringTemplateSource("test", content)).parse();
        assertEquals(1, document.nodes.size());

        Element script = (Element) document.nodes.get(0);
        assertEquals("script", script.name);
        assertTrue(script.nodes.isEmpty());
    }
}
