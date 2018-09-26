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
    void appendWithMask() {
        var headers = new HeaderMap();
        HttpString name = new HttpString("SessionId");
        headers.put(name, "value");

        var param = new HeaderLogParam(name, headers.get(name));
        var builder = new StringBuilder();
        param.append(builder, Set.of("SessionId"), 1000);
        assertThat(builder.toString()).isEqualTo("******");
    }

    @Test
    void append() {
        var headers = new HeaderMap();
        HttpString name = new HttpString("client-id");

        headers.put(name, "client1");
        var builder = new StringBuilder();
        var param = new HeaderLogParam(name, headers.get(name));
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString()).isEqualTo("client1");

        headers.add(name, "client2");
        builder = new StringBuilder();
        param = new HeaderLogParam(name, headers.get(name));
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString()).isEqualTo("[client1, client2]");
    }
}
