package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.validate.Length;

import java.time.LocalDateTime;

/**
 * @author neo
 */
@Table(name = "auto_increment_id_entity")
public class AutoIncrementIdEntity {
    @PrimaryKey(autoIncrement = true)
    @Column(name = "id")
    public Integer id;

    @Length(max = 20)
    @Column(name = "string_field")
    public String stringField;

    @Column(name = "double_field")
    public Double doubleField;

    @Column(name = "enum_field")
    public TestEnum enumField;

    @Column(name = "date_time_field")
    public LocalDateTime dateTimeField;
}
