package core.framework.impl.mongo;

import core.framework.api.util.ClasspathResources;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class EntityDecoderBuilderTest {
    @Test
    public void decode() {
        EntityDecoderBuilder<TestEntity> builder = new EntityDecoderBuilder<>(TestEntity.class);
        EntityDecoder<TestEntity> decoder = builder.build();

        verifyGeneratedMethods(builder);

        String entityJSON = ClasspathResources.text("mongo-test/entity.json");

        TestEntity entity = decoder.decode(new JsonReader(entityJSON));

        assertEquals(new ObjectId("5627b47d54b92d03adb9e9cf"), entity.id);
        assertEquals("string", entity.stringField);
        assertEquals(TestEntityChild.TestEnum.ITEM1, entity.child.enumField);
        assertEquals(2, entity.listField.size());
        assertEquals("V1", entity.listField.get(0));
        assertEquals("V2", entity.listField.get(1));
        Assert.assertNull(entity.nullChild);

        assertEquals("V1", entity.mapField.get("K1"));
        assertEquals("V2", entity.mapField.get("K2"));
    }

    private void verifyGeneratedMethods(EntityDecoderBuilder<TestEntity> builder) {
        String code = ClasspathResources.text("mongo-test/decoder-code.txt").replaceAll("\r\n", "\n");

        StringBuilder stringBuilder = new StringBuilder();
        builder.fields.forEach(stringBuilder::append);
        builder.methods.values().forEach(stringBuilder::append);

        assertEquals(code, stringBuilder.toString());
    }
}