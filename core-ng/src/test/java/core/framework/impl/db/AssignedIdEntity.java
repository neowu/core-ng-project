package core.framework.impl.db;

import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;
import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author neo
 */
@Table(name = "assigned_id_entity")
public class AssignedIdEntity {
    public static final String COLUMN_ID = "id";

    @PrimaryKey
    @Column(name = COLUMN_ID)
    @Length(max = 36)
    public String id;

    @Length(max = 20)
    @Column(name = "string_field")
    public String stringField;

    @NotNull
    @Column(name = "int_field")
    public Integer intField;

    @Column(name = "big_decimal_field")
    public BigDecimal bigDecimalField;

    @Column(name = "date_field")
    public LocalDate dateField;
}
