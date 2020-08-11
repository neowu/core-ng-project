package core.framework.mongo.impl;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Filters;
import core.framework.mongo.MongoEnumValue;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BsonLogParamTest {
    @Test
    void append() {
        var param = new BsonLogParam(Filters.eq("field", "value"), MongoClientSettings.getDefaultCodecRegistry());
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString()).isEqualTo("{\"field\": \"value\"}");
    }

    @Test
    void logValue() {
        assertThat(new BsonLogParam(Filters.eq("field", TestUnregisteredEnum.V1), MongoClientSettings.getDefaultCodecRegistry()).logValue())
                .contains("V1");
        assertThat(new BsonLogParam(null, MongoClientSettings.getDefaultCodecRegistry()).logValue())
                .isEqualTo("null");
    }

    public enum TestUnregisteredEnum {
        @MongoEnumValue("V1")
        V1
    }
}
