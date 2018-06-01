package core.framework.mongo.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.framework.util.ClasspathResources;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.util.Sets;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(ClasspathResources.text("mongo-test/entity-encoder.java"), sourceCode);
    }

    @Test
    void encode() throws IOException {
        assertEquals(Sets.newHashSet(TestEntityChild.TestEnum.class), builder.enumCodecFields.keySet());

        StringWriter writer = new StringWriter();
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId("5627b47d54b92d03adb9e9cf");
        entity.booleanField = true;
        entity.longField = 325L;
        entity.stringField = "string";
        entity.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.of(2016, 9, 1, 11, 0, 0), ZoneId.of("America/New_York"));
        entity.child = new TestEntityChild();
        entity.child.enumField = TestEntityChild.TestEnum.ITEM1;
        entity.child.enumListField = Lists.newArrayList(TestEntityChild.TestEnum.ITEM2);
        entity.listField = List.of("V1", "V2");
        entity.mapField = Maps.newHashMap();
        entity.mapField.put("K1", "V1");
        entity.mapField.put("K2", "V2");

        encoder.encode(new JsonWriter(writer, JsonWriterSettings.builder().indent(true).build()), entity);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedEntityNode = mapper.readTree(ClasspathResources.text("mongo-test/entity.json"));
        JsonNode entityNode = mapper.readTree(writer.toString());

        assertEquals(expectedEntityNode, entityNode);
    }
}
