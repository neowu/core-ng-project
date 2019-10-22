package core.framework.internal.template.expression;

import core.framework.internal.template.TemplateContext;
import core.framework.internal.template.TemplateMetaContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ExpressionBuilderTest {
    @Test
    void eval() {
        TemplateMetaContext context = new TemplateMetaContext(TestModel.class);

        TestModel root = new TestModel();
        root.stringField = "value";
        root.mapListField = Map.of("key", List.of(1));

        ExpressionHolder expression = new ExpressionBuilder("stringField", context, null).build();
        assertThat(expression.eval(new TemplateContext(root, null)))
            .isEqualTo(root.stringField);

        expression = new ExpressionBuilder("mapListField.get(\"key\")", context, null).build();
        assertThat(expression.eval(new TemplateContext(root, null)))
            .isEqualTo(root.mapListField.get("key"));
    }

    public static class TestModel {
        public String stringField;
        public Map<String, List<Integer>> mapListField;
    }
}
