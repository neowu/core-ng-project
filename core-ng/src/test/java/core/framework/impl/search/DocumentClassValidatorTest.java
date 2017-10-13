package core.framework.impl.search;

import core.framework.api.json.Property;
import core.framework.api.search.Index;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class DocumentClassValidatorTest {
    @Test
    public void validate() {
        new DocumentClassValidator(TestDocument.class).validate();
    }

    @Index(index = "main", type = "test")
    public static class TestDocument {
        @Property(name = "date_time_field")
        public LocalDateTime dateTimeField;

        @Property(name = "string_field")
        public String stringField;

        @Property(name = "list_field")
        public List<String> listField;

        @Property(name = "map_field")
        public Map<String, String> mapField;
    }
}
