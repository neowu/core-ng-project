package core.framework.mongo.impl;

import com.mongodb.client.model.Indexes;
import core.framework.inject.Inject;
import core.framework.mongo.IntegrationTest;
import core.framework.mongo.Mongo;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MongoIntegrationTest extends IntegrationTest {
    @Inject
    Mongo mongo;

    @Test
    void createIndex() {
        mongo.createIndex("entity", Indexes.ascending("string_field"));
    }

    @Test
    void dropIndex() {
        mongo.createIndex("entity", Indexes.ascending("zoned_date_time_field"));
        mongo.dropIndex("entity", Indexes.ascending("zoned_date_time_field"));
        mongo.dropIndex("entity", Indexes.ascending("zoned_date_time_field"));
    }

    @Test
    void runCommand() {
        Document result = mongo.runCommand(new Document("buildInfo", 1));
        assertThat(result.get("ok")).isEqualTo(1.0);
    }

    @Test
    void runAdminCommand() {
        Document result = mongo.runAdminCommand(new Document("serverStatus", 1));
        assertThat(result.get("ok")).isEqualTo(1.0);
    }
}
