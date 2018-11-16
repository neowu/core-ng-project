package core.framework.mongo.impl;

import core.framework.util.ClasspathResources;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityEncoderBuilderTest {
    private EntityEncoderBuilder<TestEntity> builder;
    private EntityEncoder<TestEntity> encoder;

    @BeforeAll
    void createEncoder() {
        builder = new EntityEncoderBuilder<>(TestEntity.class);
        encoder = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualTo(ClasspathResources.text("mongo-test/entity-encoder.java"));
    }

    @Test
    void encode() {
        assertThat(builder.enumCodecFields.keySet()).containsExactly(TestChildEntity.TestEnum.class);

        TestEntity entity = new TestEntity();
        entity.id = new ObjectId("5627b47d54b92d03adb9e9cf");
        entity.booleanField = Boolean.TRUE;
        entity.longField = 325L;
        entity.stringField = "string";
        entity.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.of(2016, 9, 1, 11, 0, 0), ZoneId.of("America/New_York"));
        entity.child = new TestChildEntity();
        entity.child.enumField = TestChildEntity.TestEnum.ITEM1;
        entity.child.enumListField = List.of(TestChildEntity.TestEnum.ITEM2);
        entity.listField = List.of("V1", "V2");
        entity.mapField = Map.of("K1", "V1", "K2", "V2");

        var bson = new BsonDocument();
        try (var writer = new BsonDocumentWriter(bson)) {
            encoder.encode(writer, entity);
        }

        var expectedBSON = new BsonDocument();
        try (var writer = new BsonDocumentWriter(expectedBSON)) {
            writer.pipe(new JsonReader(ClasspathResources.text("mongo-test/entity.json")));
        }

        assertThat(bson).isEqualTo(expectedBSON);
    }
}
