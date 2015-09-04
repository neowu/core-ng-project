package core.framework.impl.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.framework.api.util.ClasspathResources;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class EntityEncoderBuilderTest {
    @Test
    public void encode() throws IOException {
        EntityEncoderBuilder<TestEntity> builder = new EntityEncoderBuilder<>(TestEntity.class);
        EntityEncoder<TestEntity> encoder = builder.build();

        verifyGeneratedMethods(builder);

        StringWriter writer = new StringWriter();
        TestEntity entity = new TestEntity();
        entity.stringField = "string";
        entity.child = new TestEntityChild();
        entity.child.enumField = TestEntityChild.TestEnum.ITEM1;
        entity.listField = new ArrayList<>();
        entity.listField.add("V1");
        entity.listField.add("V2");
        entity.mapField = new HashMap<>();
        entity.mapField.put("K1", "V1");
        entity.mapField.put("K2", "V2");

        encoder.encode(new JsonWriter(writer, new JsonWriterSettings(true)), entity);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedEntityNode = mapper.readTree(ClasspathResources.text("mongo-test/entity.json"));
        JsonNode entityNode = mapper.readTree(writer.toString());

        Assert.assertEquals(expectedEntityNode, entityNode);
    }

    private void verifyGeneratedMethods(EntityEncoderBuilder<TestEntity> builder) {
        String methods = ClasspathResources.text("mongo-test/encode-methods.txt");

        builder.methods.values()
            .forEach(method -> assertThat(methods, containsString(method)));
    }
}