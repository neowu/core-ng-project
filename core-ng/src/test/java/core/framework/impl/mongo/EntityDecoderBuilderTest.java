package core.framework.impl.mongo;

import core.framework.api.util.ClasspathResources;
import org.bson.json.JsonReader;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("string", entity.stringField);
        Assert.assertEquals(TestEntityChild.TestEnum.ITEM1, entity.child.enumField);
        Assert.assertEquals(2, entity.listField.size());
        Assert.assertEquals("V1", entity.listField.get(0));
        Assert.assertEquals("V2", entity.listField.get(1));
        Assert.assertNull(entity.nullChild);

        Assert.assertEquals("V1", entity.mapField.get("K1"));
        Assert.assertEquals("V2", entity.mapField.get("K2"));
    }

    private void verifyGeneratedMethods(EntityDecoderBuilder<TestEntity> builder) {
        String methods = ClasspathResources.text("mongo-test/decode-methods.txt");

        builder.methods.values()
            .forEach(method -> Assert.assertThat(methods, CoreMatchers.containsString(method)));
    }
}