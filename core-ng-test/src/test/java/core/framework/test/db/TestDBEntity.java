package core.framework.test.db;

import core.framework.db.Column;
import core.framework.db.DBEnumValue;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

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

    @Column(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;

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
