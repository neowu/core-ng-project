package core.framework.mongo.impl;

import core.framework.util.ClasspathResources;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

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
        assertThat(sourceCode).isEqualTo(ClasspathResources.text("mongo-test/entity-decoder.java"));
    }

    @Test
    void decode() {
        String entityJSON = ClasspathResources.text("mongo-test/entity.json");

        TestEntity entity = decoder.decode(new JsonReader(entityJSON));

        assertThat(entity.id).isEqualTo(new ObjectId("5627b47d54b92d03adb9e9cf"));
        assertThat(entity.stringField).isEqualTo("string");
        assertThat(entity.longField).isEqualTo(325);
        assertThat(entity.bigDecimalField).isEqualTo("12.34");
        assertThat(entity.zonedDateTimeField).isEqualTo("2016-09-01T15:00:00Z");
        assertThat(entity.dateField).isEqualTo("2022-07-06");
        assertThat(entity.child.enumField).isEqualTo(TestEnum.ITEM1);
        assertThat(entity.listField).containsExactly("V1", "V2");
        assertThat(entity.nullChild).isNull();

        assertThat(entity.mapField).containsOnly(entry("K1", "V1"), entry("K2", "V2"));
        assertThat(entity.enumMapField).containsEntry(TestEnum.ITEM1, "V1");
        assertThat(entity.mapListField).containsOnly(entry("K1", List.of("V1")), entry("K2", List.of("V2", "V3")));
    }
}
