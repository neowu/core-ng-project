package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.util.Lists;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.DynamicInstanceBuilder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

/**
 * @author neo
 */
final class InsertQuery<T> {
    public final String sql;
    private final Function<T, Object[]> paramBuilder;

    InsertQuery(Class<T> entityClass) {
        List<Field> paramFields = Lists.newArrayList();

        StringBuilder builder = new StringBuilder("INSERT INTO ");

        Table table = entityClass.getDeclaredAnnotation(Table.class);
        builder.append(table.name()).append(" (");

        Field[] fields = entityClass.getFields();
        int index = 0;
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            if (primaryKey != null && primaryKey.autoIncrement()) continue;

            Column column = field.getDeclaredAnnotation(Column.class);
            if (index > 0) builder.append(", ");
            builder.append(column.name());
            paramFields.add(field);
            index++;
        }

        builder.append(") VALUES (");
        for (int i = 0; i < paramFields.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append('?');
        }
        builder.append(')');

        sql = builder.toString();

        paramBuilder = paramBuilder(entityClass, paramFields);
    }

    private Function<T, Object[]> paramBuilder(Class<T> entityClass, List<Field> paramFields) {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public Object apply(Object value) {\n")
            .indent(1).append("{} entity = ({}) value;", entityClass.getCanonicalName(), entityClass.getCanonicalName())
            .indent(1).append("Object[] params = new Object[{}];\n", paramFields.size());

        int index = 0;
        for (Field paramField : paramFields) {
            builder.indent(1).append("params[{}] = entity.{};\n", index, paramField.getName());
            index++;
        }

        builder.append("return params;\n")
            .append("}");

        return new DynamicInstanceBuilder<Function<T, Object[]>>(Function.class, InsertQuery.class.getCanonicalName() + "$" + entityClass.getSimpleName() + "$InsertQueryParamBuilder")
            .addMethod(builder.build())
            .build();
    }

    Object[] params(T entity) {
        return paramBuilder.apply(entity);
    }
}
