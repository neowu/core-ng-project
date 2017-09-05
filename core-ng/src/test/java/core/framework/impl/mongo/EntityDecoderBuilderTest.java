package core.framework.impl.mongo;

import core.framework.api.util.ClasspathResources;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author neo
 */
public class EntityDecoderBuilderTest {
    @Test
    public void decode() {
        EntityDecoderBuilder<TestEntity> builder = new EntityDecoderBuilder<>(TestEntity.class);
        EntityDecoder<TestEntity> decoder = builder.build();

        assertEquals(ClasspathResources.text("mongo-test/entity-decoder.java"), builder.builder.sourceCode());

        String entityJSON = ClasspathResources.text("mongo-test/entity.json");

        TestEntity entity = decoder.decode(new JsonReader(entityJSON));

        assertEquals(new ObjectId("5627b47d54b92d03adb9e9cf"), entity.id);
        assertEquals("string", entity.stringField);
        assertEquals(Long.valueOf(325), entity.longField);
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2016, 9, 1, 11, 0, 0), ZoneId.of("America/New_York")).toInstant(), entity.zonedDateTimeField.toInstant());
        assertEquals(TestEntityChild.TestEnum.ITEM1, entity.child.enumField);
        assertEquals(2, entity.listField.size());
        assertEquals("V1", entity.listField.get(0));
        assertEquals("V2", entity.listField.get(1));
        assertNull(entity.nullChild);

        assertEquals("V1", entity.mapField.get("K1"));
        assertEquals("V2", entity.mapField.get("K2"));
    }
}
