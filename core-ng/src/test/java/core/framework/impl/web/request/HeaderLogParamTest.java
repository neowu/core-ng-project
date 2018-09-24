package core.framework.impl.web.request;

import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HeaderLogParamTest {
    @Test
    void filterWithMask() {
        var headers = new HeaderMap();
        HttpString name = new HttpString("SessionId");
        headers.put(name, "value");

        var param = new HeaderLogParam(name, headers.get(name));
        assertThat(param.filter(Set.of("SessionId"))).isEqualTo("******");
    }

    @Test
    void filter() {
        var headers = new HeaderMap();
        HttpString name = new HttpString("client-id");

        headers.put(name, "client1");
        assertThat(new HeaderLogParam(name, headers.get(name)).filter(Set.of()))
                .isEqualTo("client1");

        headers.add(name, "client2");
        assertThat(new HeaderLogParam(name, headers.get(name)).filter(Set.of()))
                .isEqualTo("[client1, client2]");
    }
}
