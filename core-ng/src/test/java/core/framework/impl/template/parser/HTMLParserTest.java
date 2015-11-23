package core.framework.impl.template.parser;

import core.framework.impl.template.node.Document;
import core.framework.impl.template.node.Element;
import core.framework.impl.template.node.Text;
import core.framework.impl.template.source.StringTemplateSource;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class HTMLParserTest {
    @Test
    public void voidElements() {
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
        Assert.assertFalse(img.hasEndTag);
        assertEquals("//img.png", img.attributes.attributes.get("src").value);

        Text text = (Text) div.nodes.get(1);
        assertEquals("text", text.content);
    }

    @Test
    public void emptyScript() {
        String content = "<script type=\"text/javascript\"></script>";

        Document document = new HTMLParser(new StringTemplateSource("test", content)).parse();
        assertEquals(1, document.nodes.size());

        Element script = (Element) document.nodes.get(0);
        assertEquals("script", script.name);
        assertTrue(script.nodes.isEmpty());
    }
}