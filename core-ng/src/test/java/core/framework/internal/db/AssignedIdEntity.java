package core.framework.internal.db;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
@Table(name = "assigned_id_entity")
public class AssignedIdEntity {
    public static final String COLUMN_ID = "id";

    @PrimaryKey
    @Column(name = COLUMN_ID)
    @Size(max = 36)
    public String id;

    @Size(max = 20)
    @Column(name = "string_field")
    public String stringField;

    @NotNull
    @Column(name = "int_field")
    public Integer intField;

    @Column(name = "big_decimal_field")
    public BigDecimal bigDecimalField;

    @Column(name = "date_field")
    public LocalDate dateField;

    @Column(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;
}
