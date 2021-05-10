package core.framework.internal.web.api;

import core.framework.internal.web.service.TestWebService;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIDefinitionBuilderTest {
    private APIDefinitionBuilder builder;
    private APIDefinitionV2Builder builderV2;

    @BeforeEach
    void createAPIDefinitionBuilder() {
        builder = new APIDefinitionBuilder();
        builderV2 = new APIDefinitionV2Builder(Set.of(TestWebService.class), Set.of());
    }

    @Test
    void build() {
        builder.addServiceInterface(TestWebService.class);
        APIDefinitionResponse response = builder.build();
        APIDefinitionResponse expectedResponse = JSON.fromJSON(APIDefinitionResponse.class, ClasspathResources.text("api-test/test-webservice.json"));
        assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    void buildV2() {
        APIDefinitionV2Response response = builderV2.build();
        APIDefinitionV2Response expectedResponse = JSON.fromJSON(APIDefinitionV2Response.class, ClasspathResources.text("api-test/test-webservice-v2.json"));
        assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
    }
}
