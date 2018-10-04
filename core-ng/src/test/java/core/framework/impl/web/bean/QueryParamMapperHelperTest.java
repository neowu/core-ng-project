package core.framework.impl.web.bean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class QueryParamMapperHelperTest {
    @Test
    void deserializeString() {
        assertThat(QueryParamMapperHelper.toString("")).as("empty string will be treated as null").isNull();
        assertThat(QueryParamMapperHelper.toString(" ")).isEqualTo(" ");
        assertThat(QueryParamMapperHelper.toString("value")).isEqualTo("value");
    }
}
