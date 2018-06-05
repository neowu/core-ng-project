package core.framework.impl.web.api;

import core.framework.impl.web.service.TestWebService;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIDefinitionBuilderTest {
    private APIDefinitionBuilder builder;

    @BeforeEach
    void createAPIDefinitionBuilder() {
        builder = new APIDefinitionBuilder();
    }

    @Test
    void build() {
        builder.addServiceInterface(TestWebService.class);
        APIDefinitionResponse response = builder.build();
        APIDefinitionResponse expectedResponse = JSON.fromJSON(APIDefinitionResponse.class, ClasspathResources.text("api-test/test-webservice.json"));
        assertThat(response).isEqualToComparingFieldByFieldRecursively(expectedResponse);
    }
}
