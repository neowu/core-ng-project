package core.framework.test.db;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;

import java.util.List;

/**
 * @author chi
 */
@Table(name = "test_entity_with_json")
public class TestDBEntityWithJSON {
    @PrimaryKey
    @Column(name = "id")
    public String id;

    @Column(name = "json", json = true)
    public TestJSON jsonField;

    @Column(name = "enum_list", json = true)
    public List<TestEnum> enumList;

    @Column(name = "int_list", json = true)
    public List<Integer> intList;

    public enum TestEnum {
        @Property(name = "VALUE1")
        VALUE1,
        @Property(name = "VALUE2")
        VALUE2
    }

    public static class TestJSON {
        @NotNull
        @Property(name = "data")
        public String data;
    }
}
