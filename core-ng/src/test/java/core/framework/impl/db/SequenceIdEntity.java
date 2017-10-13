package core.framework.impl.db;

import core.framework.api.validate.Length;
import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;

/**
 * @author neo
 */
@Table(name = "sequence_id_entity")
public class SequenceIdEntity {
    @PrimaryKey(sequence = "seq")
    @Column(name = "id")
    public Integer id;

    @Length(max = 20)
    @Column(name = "string_field")
    public String stringField;

    @Column(name = "long_field")
    public Long longField;
}
