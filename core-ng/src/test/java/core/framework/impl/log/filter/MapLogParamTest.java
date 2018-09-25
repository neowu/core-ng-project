package core.framework.impl.log.filter;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MapLogParamTest {
    @Test
    void append() {
        var param = new MapLogParam(Map.of("SessionId", "123", "key1", "value1"));
        var builder = new StringBuilder();
        param.append(builder, Set.of("SessionId"));
        assertThat(builder.toString()).contains("SessionId=******").contains("key1=value1");
    }
}
