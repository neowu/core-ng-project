package core.framework.impl.template;

import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Lists;
import core.framework.impl.template.source.TemplateSource;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class TemplateTest {
    @Test
    public void process() {
        Template template = new TemplateBuilder(new TestTemplateSource("template-test/template.html"), TestModel.class).build();

        TestModel model = new TestModel();
        model.stringField = "string<";
        model.numberField = 100;
        model.items.addAll(Lists.newArrayList("a", "b", "c"));
        model.children.add(child("child1", 1.0, true));
        model.children.add(child("child2", 2.0, false));
        model.htmlField = "<pre>html</pre>";

        String result = template.process(new CallStack(model));

        Assert.assertEquals(ClasspathResources.text("template-test/template-result.html").replaceAll("\r\n", "\n"), result);
    }

    private TestModelChild child(String stringField, Double doubleField, Boolean booleanField) {
        TestModelChild child = new TestModelChild();
        child.stringField = stringField;
        child.doubleField = doubleField;
        child.booleanField = booleanField;
        return child;
    }

    static class TestTemplateSource implements TemplateSource {
        private final String path;

        TestTemplateSource(String path) {
            this.path = path;
        }

        @Override
        public String content() {
            return ClasspathResources.text(path);
        }

        @Override
        public TemplateSource resolve(String path) {
            if ("footer.html".equals(path)) return new TestTemplateSource("template-test/footer.html");
            Assert.fail("unknown path, path=" + path);
            return null;
        }

        @Override
        public String source() {
            return path;
        }
    }
}