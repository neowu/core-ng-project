package core.framework.internal.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.asm.DynamicInstanceBuilder;
import core.framework.internal.reflect.Classes;
import core.framework.util.Lists;
import core.framework.util.Strings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static core.framework.internal.asm.Literal.type;

/**
 * @author neo
 */
class InsertQueryBuilder<T> {
    final DynamicInstanceBuilder<InsertQueryParamBuilder<T>> builder;
    private final Class<T> entityClass;
    private final Dialect dialect;
    private final List<String> primaryKeyFieldNames = Lists.newArrayList();

    private String generatedColumn;
    private List<ParamField> paramFields;
    private String insertSQL;
    private String insertIgnoreSQL;
    private String upsertSQL;

    InsertQueryBuilder(Class<T> entityClass, Dialect dialect) {
        this.entityClass = entityClass;
        this.dialect = dialect;
        builder = new DynamicInstanceBuilder<>(InsertQueryParamBuilder.class, entityClass.getSimpleName());
    }

    InsertQuery<T> build() {
        buildSQL();
        builder.addMethod(applyMethod());
        InsertQueryParamBuilder<T> paramBuilder = builder.build();
        return new InsertQuery<>(insertSQL, insertIgnoreSQL, upsertSQL, generatedColumn, paramBuilder);
    }

    private void buildSQL() {
        List<Field> fields = Classes.instanceFields(entityClass);
        int size = fields.size();
        paramFields = new ArrayList<>(size);
        List<String> columns = new ArrayList<>(size);
        List<String> params = new ArrayList<>(size);
        List<String> updates = new ArrayList<>(size);
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            Column column = field.getDeclaredAnnotation(Column.class);
            String columnName = column.name();
            if (primaryKey != null) {
                if (primaryKey.autoIncrement()) {
                    generatedColumn = columnName;
                    continue;
                }
                primaryKeyFieldNames.add(field.getName());    // pk fields is only needed for assigned id
            } else {
                if (dialect == Dialect.MYSQL) {
                    // VALUES(column) syntax is deprecated since MySQL 8.0.20, this is to keep compatible with old MySQL (gcloud still has 8.0.18)
                    // will update to new syntax in future
                    updates.add(Strings.format("{} = VALUES({})", columnName, columnName));
                } else if (dialect == Dialect.POSTGRESQL) {
                    updates.add(Strings.format("{} = EXCLUDED.{}", columnName, columnName));
                }
            }
            paramFields.add(new ParamField(field.getName(), column.json()));
            columns.add(columnName);
            params.add("?");
        }

        var builder = new CodeBuilder()
            .append("INSERT INTO {} (", entityClass.getDeclaredAnnotation(Table.class).name())
            .appendCommaSeparatedValues(columns)
            .append(") VALUES (")
            .appendCommaSeparatedValues(params)
            .append(')');
        insertSQL = builder.build();

        if (generatedColumn != null) return;  // auto-increment entity doesn't need insert ignore and upsert, refer to core.framework.internal.db.RepositoryImpl.insertIgnore

        if (dialect == Dialect.MYSQL) {
            insertIgnoreSQL = new StringBuilder(insertSQL).insert(6, " IGNORE").toString();
        } else if (dialect == Dialect.POSTGRESQL) {
            insertIgnoreSQL = insertSQL + " ON CONFLICT DO NOTHING";
        }

        if (dialect == Dialect.MYSQL) {
            builder.append(" ON DUPLICATE KEY UPDATE ")
                .appendCommaSeparatedValues(updates);
        } else {
            builder.append(" ON CONFLICT (").appendCommaSeparatedValues(primaryKeyFieldNames).append(") DO UPDATE SET ")
                .appendCommaSeparatedValues(updates);
        }
        upsertSQL = builder.build();
    }

    private String applyMethod() {
        var builder = new CodeBuilder();

        String entityClassLiteral = type(entityClass);
        builder.append("public Object[] params(Object value) {\n")
            .indent(1).append("{} entity = ({}) value;\n", entityClassLiteral, entityClassLiteral);

        if (generatedColumn == null) {
            for (String name : primaryKeyFieldNames) {
                builder.indent(1).append("if (entity.{} == null) throw new Error(\"primary key must not be null, field={}\");\n", name, name);
            }
        }

        builder.indent(1).append("Object[] params = new Object[{}];\n", paramFields.size());
        int index = 0;
        for (ParamField field : paramFields) {
            if (field.json) {
                builder.indent(1).append("params[{}] = {}.toJSON(entity.{});\n", index, type(JSONHelper.class), field.name);
            } else {
                builder.indent(1).append("params[{}] = entity.{};\n", index, field.name);
            }
            index++;
        }

        builder.indent(1).append("return params;\n")
            .append("}");

        return builder.build();
    }

    private record ParamField(String name, boolean json) {
    }
}
