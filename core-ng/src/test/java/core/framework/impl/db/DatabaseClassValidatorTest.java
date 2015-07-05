package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.validate.Length;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * @author neo
 */
public class DatabaseClassValidatorTest {
    @Table(name = "table")
    public static class Entity {
        @PrimaryKey(autoIncrement = true)
        @Column(name = "id")
        public Integer id;

        @Length(max = 10)
        @Column(name = "string_column")
        public String stringColumn;

        @Column(name = "boolean_column")
        public Boolean booleanColumn;

        @Column(name = "date_time_column")
        public LocalDateTime dateTimeColumn;
    }

    @Test
    public void validateEntityClass() {
        new DatabaseClassValidator(Entity.class).validateEntityClass();
    }
}