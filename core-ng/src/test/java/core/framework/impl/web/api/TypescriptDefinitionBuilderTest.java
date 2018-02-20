package core.framework.impl.web.api;

import core.framework.impl.web.service.TestWebService;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TypescriptDefinitionBuilderTest {
    @Test
    void build() {
        TypescriptDefinitionBuilder builder = new TypescriptDefinitionBuilder();
        builder.addServiceInterface(TestWebService.class);
        String definition = builder.build();

        assertThat(definition).isEqualToIgnoringWhitespace(ClasspathResources.text("api-test/test-webservice.ts"));
    }
}
