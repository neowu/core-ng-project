package core.framework.impl.template;

import core.framework.impl.template.source.ClasspathTemplateSource;
import core.framework.util.ClasspathResources;
import core.framework.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class HTMLTemplateTest {
    @Test
    void process() {
        HTMLTemplateBuilder builder = new HTMLTemplateBuilder(new ClasspathTemplateSource("template-test/template.html"), TestModel.class);
        builder.message = key -> Optional.of(key + "_value");
        HTMLTemplate template = builder.build();

        TestModel model = new TestModel();
        model.stringField = "string<";
        model.numberField = 100;
        model.items.addAll(Lists.newArrayList("a", "b", "c"));
        model.children.add(child("child1", 1.0, true));
        model.children.add(child("child2", 2.0, false));
        model.htmlField = "<pre>html</pre>";

        String result = template.process(new TemplateContext(model, new CDNManager()));

        assertEquals(ClasspathResources.text("template-test/template-result.html").replaceAll("\r\n", "\n"), result);
    }

    private TestModelChild child(String stringField, Double doubleField, Boolean booleanField) {
        TestModelChild child = new TestModelChild();
        child.stringField = stringField;
        child.doubleField = doubleField;
        child.booleanField = booleanField;
        return child;
    }
}
