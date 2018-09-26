package core.framework.mongo.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BsonLogParamTest {
    @Test
    void append() {
        var param = new BsonLogParam(Filters.eq("field", "value"), MongoClient.getDefaultCodecRegistry());
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString()).isEqualTo("{ \"field\" : \"value\" }");
    }
}
