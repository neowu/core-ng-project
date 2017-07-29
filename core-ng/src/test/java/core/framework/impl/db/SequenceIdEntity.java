package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.validate.Length;

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
}
