package core.framework.internal.db;

import core.framework.api.validate.Size;
import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
@Table(name = "auto_increment_id_entity")
public class AutoIncrementIdEntity {
    @PrimaryKey(autoIncrement = true)
    @Column(name = "id")
    public Integer id;

    @Size(max = 20)
    @Column(name = "string_field")
    public String stringField;

    @Column(name = "double_field")
    public Double doubleField;

    @Column(name = "enum_field")
    public TestEnum enumField;

    @Column(name = "date_time_field")
    public LocalDateTime dateTimeField;

    @Column(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;
}
