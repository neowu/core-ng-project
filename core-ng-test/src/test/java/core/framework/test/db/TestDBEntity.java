package core.framework.test.db;

import core.framework.api.db.Column;
import core.framework.api.db.DBEnumValue;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author chi
 */
@Table(name = "test_entity")
public class TestDBEntity {
    @PrimaryKey
    @Column(name = "id")
    public String id;

    @Column(name = "int_field")
    public Integer intField;

    @Column(name = "string_field")
    public String stringField;

    @Column(name = "date_time_field")
    public LocalDateTime dateTimeField;

    @Column(name = "date_field")
    public LocalDate dateField;

    @Column(name = "double_field")
    public Double doubleField;

    @Column(name = "enum_field")
    public TestEnum enumField;

    @Column(name = "bool_field")
    public Boolean boolField;

    public enum TestEnum {
        @DBEnumValue("VALUE1")
        VALUE1,
        @DBEnumValue("VALUE2")
        VALUE2
    }
}
