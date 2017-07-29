package core.framework.test.db;

import core.framework.api.db.Column;
import core.framework.api.db.Database;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;
import core.framework.impl.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author neo
 */
public final class EntitySchemaGenerator {
    private final Logger logger = LoggerFactory.getLogger(EntitySchemaGenerator.class);

    private final Database database;
    private final Class<?> entityClass;

    public EntitySchemaGenerator(Database database, Class<?> entityClass) {
        this.database = database;
        this.entityClass = entityClass;
    }

    public void generate() {
        StopWatch watch = new StopWatch();
        List<String> statements = schemeStatements();
        try {
            for (String statement : statements) {
                database.execute(statement);
            }
        } finally {
            logger.info("create schema, entityClass={}, sql={}, elapsedTime={}", entityClass.getCanonicalName(), statements, watch.elapsedTime());
        }
    }

    private List<String> schemeStatements() {
        List<String> statements = Lists.newArrayList();

        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        Table table = entityClass.getDeclaredAnnotation(Table.class);
        builder.append(table.name()).append(" (");

        List<String> primaryKeys = Lists.newArrayList();
        String sequence = null;
        for (Field field : Classes.instanceFields(entityClass)) {
            Column column = field.getDeclaredAnnotation(Column.class);
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);

            builder.append(column.name()).append(' ');
            builder.append(columnType(field.getType(), field.getDeclaredAnnotation(Length.class)));

            if (primaryKey != null) {
                if (primaryKey.autoIncrement()) builder.append(" AUTO_INCREMENT");
                if (!Strings.isEmpty(primaryKey.sequence())) sequence = primaryKey.sequence();
                primaryKeys.add(column.name());
            }

            if (field.isAnnotationPresent(NotNull.class)) {
                builder.append(" NOT NULL");
            }

            builder.append(", ");
        }

        builder.append("PRIMARY KEY(");

        int index = 0;
        for (String primaryKey : primaryKeys) {
            if (index > 0) builder.append(", ");
            builder.append(primaryKey);
            index++;
        }

        builder.append("))");

        statements.add(builder.toString());

        if (sequence != null) {
            statements.add("CREATE SEQUENCE IF NOT EXISTS " + sequence);
        }

        return statements;
    }

    // http://dev.mysql.com/doc/connector-j/en/connector-j-reference-type-conversions.html
    private String columnType(Class<?> fieldClass, Length lengthAnnotation) {
        if (Integer.class.equals(fieldClass)) return "INT";
        if (Long.class.equals(fieldClass)) return "BIGINT";
        if (String.class.equals(fieldClass)) {
            int length = 500;
            if (lengthAnnotation != null && lengthAnnotation.max() > 0) length = lengthAnnotation.max();
            return "VARCHAR(" + length + ")";
        }
        if (fieldClass.isEnum()) {
            return "VARCHAR(100)";
        }
        if (Boolean.class.equals(fieldClass)) {
            return "BIT(1)";
        }
        if (Double.class.equals(fieldClass)) {
            return "DOUBLE";
        }
        if (BigDecimal.class.equals(fieldClass)) {
            return "DECIMAL(10,2)";
        }
        if (LocalDateTime.class.equals(fieldClass) || ZonedDateTime.class.equals(fieldClass)) {
            return "TIMESTAMP";
        }
        if (LocalDate.class.equals(fieldClass)) {
            return "DATE";
        }
        throw Exceptions.error("unsupported field class, class={}", fieldClass.getCanonicalName());
    }
}
