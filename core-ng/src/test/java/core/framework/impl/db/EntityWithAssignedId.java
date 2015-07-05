package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.validate.Length;

import java.math.BigDecimal;

/**
 * @author neo
 */
@Table(name = "entity_with_assigned_id")
public class EntityWithAssignedId {
    @PrimaryKey
    @Column(name = "id")
    @Length(max = 36)
    public String id;

    @Length(max = 20)
    @Column(name = "string_field")
    public String stringField;

    @Column(name = "int_field")
    public Integer intField;

    @Column(name = "big_decimal_field")
    public BigDecimal bigDecimalField;
}
