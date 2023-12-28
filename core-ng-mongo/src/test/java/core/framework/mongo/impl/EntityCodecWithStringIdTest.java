package core.framework.mongo.impl;

import org.bson.BsonString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityCodecWithStringIdTest {
    private EntityCodec<TestEntityWithStringId> entityCodec;

    @BeforeAll
    void createEntityCodec() {
        var entityCodecs = new EntityCodecs();
        entityCodecs.registerEntity(TestEntityWithStringId.class);
        entityCodec = (EntityCodec<TestEntityWithStringId>) entityCodecs.codecRegistry().get(TestEntityWithStringId.class);
    }

    @Test
    void documentHasId() {
        assertFalse(entityCodec.documentHasId(new TestEntityWithStringId()));

        var entity = new TestEntityWithStringId();
        entity.id = "id";
        assertThat(entityCodec.documentHasId(entity)).isTrue();
    }

    @Test
    void generateIdIfAbsentFromDocument() {
        assertThatThrownBy(() -> entityCodec.generateIdIfAbsentFromDocument(new TestEntityWithStringId()))
            .isInstanceOf(Error.class)
            .hasMessageContaining("id must be assigned");
    }

    @Test
    void getDocumentId() {
        var entity = new TestEntityWithStringId();
        entity.id = "id";
        assertThat(entityCodec.getDocumentId(entity)).isEqualTo(new BsonString("id"));

        assertThat(entityCodec.getDocumentId(new TestEntityWithStringId())).isNull();
    }
}
