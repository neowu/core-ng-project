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
    void filter() {
        var param = new MapLogParam(Map.of("SessionId", "123", "key1", "value1"));
        String message = param.filter(Set.of("SessionId"));
        assertThat(message).contains("SessionId=******").contains("key1=value1");
    }
}
