package core.framework.impl.template.parser;

import core.framework.impl.template.node.Document;
import core.framework.impl.template.node.Element;
import core.framework.impl.template.node.Text;
import core.framework.impl.template.source.StringTemplateSource;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class HTMLParserTest {
    @Test
    public void parseVoidElements() {
        String content = "<html><div><img src=img.png>text</div></html>";

        Document document = new HTMLParser(new StringTemplateSource("test", content)).parse();
        Assert.assertEquals(1, document.nodes.size());

        Element html = (Element) document.nodes.get(0);
        Assert.assertEquals("html", html.name);
        Assert.assertEquals(1, html.nodes.size());

        Element div = (Element) html.nodes.get(0);
        Assert.assertEquals("div", div.name);
        Assert.assertEquals(2, div.nodes.size());
        Assert.assertTrue(div.hasEndTag);

        Element img = (Element) div.nodes.get(0);
        Assert.assertEquals("img", img.name);
        Assert.assertFalse(img.hasEndTag);
        Assert.assertEquals("img.png", img.attributes.attributes.get("src").value);

        Text text = (Text) div.nodes.get(1);
        Assert.assertEquals("text", text.content);
    }
}