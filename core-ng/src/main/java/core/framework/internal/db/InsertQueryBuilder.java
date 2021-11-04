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
    private final List<String> primaryKeyFieldNames = Lists.newArrayList();

    private String generatedColumn;
    private List<String> paramFieldNames;
    private String sql;
    private String upsertClause;

    InsertQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        builder = new DynamicInstanceBuilder<>(InsertQueryParamBuilder.class, entityClass.getSimpleName());
    }

    InsertQuery<T> build() {
        buildSQL();
        builder.addMethod(applyMethod());
        InsertQueryParamBuilder<T> paramBuilder = builder.build();
        return new InsertQuery<>(sql, upsertClause, generatedColumn, paramBuilder);
    }

    private void buildSQL() {
        List<Field> fields = Classes.instanceFields(entityClass);
        int size = fields.size();
        paramFieldNames = new ArrayList<>(size);
        List<String> columns = new ArrayList<>(size);
        List<String> params = new ArrayList<>(size);
        List<String> updates = new ArrayList<>(size);
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            String column = field.getDeclaredAnnotation(Column.class).name();
            if (primaryKey != null) {
                if (primaryKey.autoIncrement()) {
                    generatedColumn = column;
                    continue;
                }
                primaryKeyFieldNames.add(field.getName());    // pk fields is only needed for assigned id
            } else {
                // since MySQL 8.0.20, VALUES(column) syntax is deprecated, currently gcloud MySQL is still on 8.0.18
                updates.add(Strings.format("{} = VALUES({})", column, column));
            }
            paramFieldNames.add(field.getName());
            columns.add(column);
            params.add("?");
        }

        var builder = new CodeBuilder()
            .append("INSERT INTO {} (", entityClass.getDeclaredAnnotation(Table.class).name())
            .appendCommaSeparatedValues(columns)
            .append(") VALUES (")
            .appendCommaSeparatedValues(params)
            .append(')');
        sql = builder.build();

        var upsertBuilder = new CodeBuilder()
            .append(" ON DUPLICATE KEY UPDATE ")
            .appendCommaSeparatedValues(updates);
        upsertClause = upsertBuilder.build();
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

        builder.indent(1).append("Object[] params = new Object[{}];\n", paramFieldNames.size());
        int index = 0;
        for (String name : paramFieldNames) {
            builder.indent(1).append("params[{}] = entity.{};\n", index, name);
            index++;
        }

        builder.indent(1).append("return params;\n")
            .append("}");

        return builder.build();
    }
}
