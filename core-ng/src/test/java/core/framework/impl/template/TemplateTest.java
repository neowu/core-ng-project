package core.framework.impl.template;

import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author neo
 */
public class TemplateTest {
    public static class TestModel {
        public String stringField;
        public Integer numberField;
        public List<String> items = Lists.newArrayList();
        public List<Child> children = Lists.newArrayList();

        public Integer addToNumberField() {
            return numberField + 100;
        }

        public String appendToStringField(String postfix) {
            return stringField + postfix;
        }
    }

    public static class Child {
        public final String stringField;
        public final Double doubleField;
        public final Boolean boolField;

        public Child(String stringField, Double doubleField, Boolean boolField) {
            this.stringField = stringField;
            this.doubleField = doubleField;
            this.boolField = boolField;
        }
    }

    @Test
    public void process() {
        Template template = new TemplateBuilder(ClasspathResources.text("template-test/template.html"), TestModel.class).build();

        TestModel model = new TestModel();
        model.stringField = "string";
        model.numberField = 100;
        model.items.addAll(Lists.newArrayList("a", "b", "c"));
        model.children.add(new Child("child1", 1.0, true));
        model.children.add(new Child("child2", 2.0, false));

        String result = template.process(model);

        Assert.assertEquals(ClasspathResources.text("template-test/template-result.html"), result);
    }
}