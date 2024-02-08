package core.framework.internal.db;

import core.framework.api.json.Property;
import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;

import java.util.List;

/**
 * @author neo
 */
@Table(name = "json_entity")
public class JSONEntity {
    @PrimaryKey
    @Column(name = "id")
    public String id;

    @Column(name = "json", json = true)
    public TestJSON jsonField;

    @Column(name = "enum_list", json = true)
    public List<TestJSONEnum> enumList;

    @Column(name = "int_list", json = true)
    public List<Integer> intList;

    public enum TestJSONEnum {
        @Property(name = "DB_V1")
        V1,
        @Property(name = "DB_V2")
        V2
    }

    public static class TestJSON {
        @Property(name = "data")
        public String data;

    }
}
