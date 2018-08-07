package core.framework.mongo.impl;

import core.framework.util.ClasspathResources;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityDecoderBuilderTest {
    private EntityDecoderBuilder<TestEntity> builder;
    private EntityDecoder<TestEntity> decoder;

    @BeforeAll
    void createDecoder() {
        builder = new EntityDecoderBuilder<>(TestEntity.class);
        decoder = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("mongo-test/entity-decoder.java"), sourceCode);
    }

    @Test
    void decode() {
        String entityJSON = ClasspathResources.text("mongo-test/entity.json");

        TestEntity entity = decoder.decode(new JsonReader(entityJSON));

        assertEquals(new ObjectId("5627b47d54b92d03adb9e9cf"), entity.id);
        assertEquals("string", entity.stringField);
        assertEquals(Long.valueOf(325), entity.longField);
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2016, 9, 1, 11, 0, 0), ZoneId.of("America/New_York")).toInstant(), entity.zonedDateTimeField.toInstant());
        assertEquals(TestChildEntity.TestEnum.ITEM1, entity.child.enumField);
        assertEquals(2, entity.listField.size());
        assertEquals("V1", entity.listField.get(0));
        assertEquals("V2", entity.listField.get(1));
        assertNull(entity.nullChild);

        assertEquals("V1", entity.mapField.get("K1"));
        assertEquals("V2", entity.mapField.get("K2"));
    }
}
