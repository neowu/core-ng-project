package core.framework.internal.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.asm.DynamicInstanceBuilder;
import core.framework.internal.reflect.Classes;
import core.framework.util.Lists;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static core.framework.internal.asm.Literal.type;

/**
 * @author neo
 */
class InsertQueryBuilder<T> {
    final DynamicInstanceBuilder<Function<T, Object[]>> builder;
    private final Class<T> entityClass;
    private final List<Field> primaryKeyFields = Lists.newArrayList();

    private String generatedColumn;
    private List<Field> paramFields;
    private String sql;

    InsertQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        builder = new DynamicInstanceBuilder<>(Function.class, InsertQuery.class.getCanonicalName() + "$" + entityClass.getSimpleName() + "$ParamBuilder");
    }

    InsertQuery<T> build() {
        buildSQL();

        builder.addMethod(applyMethod(entityClass, paramFields));
        Function<T, Object[]> paramBuilder = builder.build();

        return new InsertQuery<>(sql, generatedColumn, paramBuilder);
    }

    private void buildSQL() {
        var builder = new StringBuilder(256).append("INSERT INTO ");

        Table table = entityClass.getDeclaredAnnotation(Table.class);
        builder.append(table.name()).append(" (");

        List<Field> fields = Classes.instanceFields(entityClass);
        paramFields = new ArrayList<>(fields.size());
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            Column column = field.getDeclaredAnnotation(Column.class);
            if (primaryKey != null) {
                if (primaryKey.autoIncrement()) {
                    generatedColumn = column.name();
                    continue;
                }
                primaryKeyFields.add(field);    // pk fields is only needed for assigned id
            }
            if (!paramFields.isEmpty()) builder.append(", ");
            builder.append(column.name());
            paramFields.add(field);
        }
        builder.append(") VALUES (");
        for (int i = 0; i < paramFields.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append('?');
        }
        builder.append(')');

        sql = builder.toString();
    }

    private String applyMethod(Class<T> entityClass, List<Field> paramFields) {
        var builder = new CodeBuilder();

        String entityClassLiteral = type(entityClass);
        builder.append("public Object apply(Object value) {\n")
               .indent(1).append("{} entity = ({}) value;\n", entityClassLiteral, entityClassLiteral);

        if (generatedColumn == null) {
            for (Field primaryKeyField : primaryKeyFields) {
                builder.indent(1).append("if (entity.{} == null) throw new Error(\"primary key must not be null, field={}\");\n", primaryKeyField.getName(), primaryKeyField.getName());
            }
        }

        builder.indent(1).append("Object[] params = new Object[{}];\n", paramFields.size());
        int index = 0;
        for (Field paramField : paramFields) {
            builder.indent(1).append("params[{}] = entity.{};\n", index, paramField.getName());
            index++;
        }

        builder.indent(1).append("return params;\n")
               .append("}");

        return builder.build();
    }
}
