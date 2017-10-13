package core.framework.template;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class HTMLTemplateEngineTest {
    HTMLTemplateEngine engine;

    @Before
    public void createHTMLTemplateEngine() {
        engine = new HTMLTemplateEngine();
    }

    @Test
    public void process() {
        engine.add("test", "<html><img c:src=\"imageURL\"></html>", TestModel.class);
        TestModel model = new TestModel();
        model.imageURL = "http://domain/image.png";
        String html = engine.process("test", model);
        Assert.assertThat(html, CoreMatchers.containsString("<img src=http://domain/image.png>"));
    }

    public static class TestModel {
        public String imageURL;
    }
}
