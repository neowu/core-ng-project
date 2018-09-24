package core.framework.mongo.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BsonLogParamTest {
    @Test
    void convertToString() {
        var param = new BsonLogParam(Filters.eq("field", "value"), MongoClient.getDefaultCodecRegistry());
        assertThat(param.toString()).isEqualTo("{ \"field\" : \"value\" }");
    }
}
