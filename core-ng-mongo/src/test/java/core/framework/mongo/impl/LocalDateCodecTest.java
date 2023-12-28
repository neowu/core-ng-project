package core.framework.mongo.impl;

import org.bson.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LocalDateCodecTest {
    @Test
    void readWithInvalidDate() {
        var reader = new JsonReader("""
            {"date": "2022"}
            """);
        reader.readStartDocument();
        String field = reader.readName();
        LocalDate value = LocalDateCodec.read(reader, field);
        assertThat(value).isNull();
    }
}
