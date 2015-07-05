package core.framework.api.template;

import core.framework.api.util.Maps;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * @author neo
 */
public class HTMLTemplateTest {
    HTMLTemplate htmlTemplate;

    @Before
    public void createHTMLTemplate() {
        htmlTemplate = new HTMLTemplate();
    }

    @Test
    public void processStringTemplate() {
        htmlTemplate.putTemplate("test", "<html></html>");
        String html = htmlTemplate.process("test", Maps.newHashMap());
        Assert.assertEquals("<html></html>", html);
    }

    @Test
    public void processClasspathTemplate() {
        Map<String, Object> context = Maps.newHashMap();
        context.put("name", "value");
        String html = htmlTemplate.process("template-test/template.html", context);
        Assert.assertThat(html, CoreMatchers.containsString("<p>value</p>"));
    }
}