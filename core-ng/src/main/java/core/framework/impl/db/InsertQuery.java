package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.util.Lists;
import core.framework.util.Strings;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import static core.framework.impl.asm.Literal.type;

/**
 * @author neo
 */
final class InsertQuery<T> {
    final String sql;
    final String generatedColumn;
    private final Function<T, Object[]> paramBuilder;

    InsertQuery(Class<T> entityClass) {
        List<Field> paramFields = Lists.newArrayList();

        StringBuilder builder = new StringBuilder("INSERT INTO ");
        StringBuilder valueClause = new StringBuilder();

        Table table = entityClass.getDeclaredAnnotation(Table.class);
        builder.append(table.name()).append(" (");

        String generatedColumn = null;
        List<Field> fields = Classes.instanceFields(entityClass);
        int startLength = builder.length();
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            Column column = field.getDeclaredAnnotation(Column.class);
            if (primaryKey != null) {
                if (primaryKey.autoIncrement()) {
                    generatedColumn = column.name();
                    continue;
                } else if (!Strings.isEmpty(primaryKey.sequence())) {
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

        this.generatedColumn = generatedColumn;

        paramBuilder = paramBuilder(entityClass, paramFields);
    }

    private void addColumn(StringBuilder builder, int startLength, String name) {
        if (builder.length() > startLength) builder.append(", ");
        builder.append(name);
    }

    private void addValue(StringBuilder valueClause, String value) {
        if (valueClause.length() > 0) valueClause.append(", ");
        valueClause.append(value);
    }

    private Function<T, Object[]> paramBuilder(Class<T> entityClass, List<Field> paramFields) {
        CodeBuilder builder = new CodeBuilder();

        String entityClassLiteral = type(entityClass);
        builder.append("public Object apply(Object value) {\n")
               .indent(1).append("{} entity = ({}) value;", entityClassLiteral, entityClassLiteral)
               .indent(1).append("Object[] params = new Object[{}];\n", paramFields.size());

        int index = 0;
        for (Field paramField : paramFields) {
            builder.indent(1).append("params[{}] = entity.{};\n", index, paramField.getName());
            index++;
        }

        builder.append("return params;\n")
               .append("}");

        DynamicInstanceBuilder<Function<T, Object[]>> dynamicInstanceBuilder = new DynamicInstanceBuilder<>(Function.class, InsertQuery.class.getCanonicalName() + "$" + entityClass.getSimpleName() + "$InsertQueryParamBuilder");
        dynamicInstanceBuilder.addMethod(builder.build());
        return dynamicInstanceBuilder.build();
    }

    Object[] params(T entity) {
        return paramBuilder.apply(entity);
    }
}
