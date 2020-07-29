package core.framework.internal.web.bean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class QueryParamHelperTest {
    @Test
    void deserializeString() {
        assertThat(QueryParamHelper.toString("")).as("empty string will be treated as null").isNull();
        assertThat(QueryParamHelper.toString(" ")).isEqualTo(" ");
        assertThat(QueryParamHelper.toString("value")).isEqualTo("value");
    }
}
