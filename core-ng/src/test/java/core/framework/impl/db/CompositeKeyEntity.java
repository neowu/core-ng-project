package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.validate.Length;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;

/**
 * @author neo
 */
@Table(name = "composite_key_entity")
public class CompositeKeyEntity {
    @PrimaryKey
    @Column(name = "id1")
    @Length(max = 36)
    public String id1;

    @PrimaryKey
    @Column(name = "id2")
    @Length(max = 36)
    public String id2;

    @NotNull
    @Column(name = "boolean_field")
    public Boolean booleanField;

    @Min(1)
    @Column(name = "long_field")
    public Long longField;
}
