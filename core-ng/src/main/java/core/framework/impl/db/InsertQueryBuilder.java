package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.util.Strings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static core.framework.impl.asm.Literal.type;

/**
 * @author neo
 */
class InsertQueryBuilder<T> {
    final DynamicInstanceBuilder<Function<T, Object[]>> builder;
    private final Class<T> entityClass;

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
        var builder = new StringBuilder("INSERT INTO ");
        var valueClause = new StringBuilder();

        Table table = entityClass.getDeclaredAnnotation(Table.class);
        builder.append(table.name()).append(" (");

        List<Field> fields = Classes.instanceFields(entityClass);
        paramFields = new ArrayList<>(fields.size());
        int startLength = builder.length();
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            Column column = field.getDeclaredAnnotation(Column.class);
            if (primaryKey != null) {
                if (primaryKey.autoIncrement()) {
                    generatedColumn = column.name();
                    continue;
                } else if (!Strings.isBlank(primaryKey.sequence())) {
                    generatedColumn = column.name();
                    addColumn(builder, startLength, column.name());
                    addValue(valueClause, primaryKey.sequence() + ".NEXTVAL");
                    continue;
                }
            }

            addColumn(builder, startLength, column.name());
            addValue(valueClause, "?");
            paramFields.add(field);
        }
        builder.append(") VALUES (").append(valueClause).append(')');
        sql = builder.toString();
    }

    private void addColumn(StringBuilder builder, int startLength, String name) {
        if (builder.length() > startLength) builder.append(", ");
        builder.append(name);
    }

    private void addValue(StringBuilder valueClause, String value) {
        if (valueClause.length() > 0) valueClause.append(", ");
        valueClause.append(value);
    }

    private String applyMethod(Class<T> entityClass, List<Field> paramFields) {
        var builder = new CodeBuilder();

        String entityClassLiteral = type(entityClass);
        builder.append("public Object apply(Object value) {\n")
               .indent(1).append("{} entity = ({}) value;\n", entityClassLiteral, entityClassLiteral)
               .indent(1).append("Object[] params = new Object[{}];\n", paramFields.size());

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
