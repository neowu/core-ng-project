package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.util.Lists;
import core.framework.api.util.Strings;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author neo
 */
final class InsertQuery<T> {
    final String sql;
    final String sequencePrimaryKey;
    private final Function<T, Object[]> paramBuilder;

    InsertQuery(Class<T> entityClass) {
        List<ParamField> paramFields = Lists.newArrayList();

        StringBuilder builder = new StringBuilder("INSERT INTO ");

        Table table = entityClass.getDeclaredAnnotation(Table.class);
        builder.append(table.name()).append(" (");

        String sequencePrimaryKey = null;

        List<Field> fields = Classes.instanceFields(entityClass);
        int index = 0;
        for (Field field : fields) {
            String sequence = null;
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            if (primaryKey != null) {
                if (primaryKey.autoIncrement()) continue;
                if (!Strings.isEmpty(primaryKey.sequence())) sequence = primaryKey.sequence();
            }

            Column column = field.getDeclaredAnnotation(Column.class);
            if (index > 0) builder.append(", ");
            builder.append(column.name());
            if (sequence != null) sequencePrimaryKey = column.name();
            paramFields.add(new ParamField(field, sequence));
            index++;
        }

        builder.append(") VALUES (");

        index = 0;
        for (ParamField paramField : paramFields) {
            if (index > 0) builder.append(", ");
            if (paramField.sequence != null) builder.append(paramField.sequence).append(".NEXTVAL");
            else builder.append('?');
            index++;
        }
        builder.append(')');

        sql = builder.toString();

        this.sequencePrimaryKey = sequencePrimaryKey;

        List<Field> params = paramFields.stream()
                                        .filter(field -> field.sequence == null)
                                        .map(field -> field.field)
                                        .collect(Collectors.toList());
        paramBuilder = paramBuilder(entityClass, params);
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

        DynamicInstanceBuilder<Function<T, Object[]>> dynamicInstanceBuilder = new DynamicInstanceBuilder<>(Function.class, InsertQuery.class.getCanonicalName() + "$" + entityClass.getSimpleName() + "$InsertQueryParamBuilder");
        dynamicInstanceBuilder.addMethod(builder.build());
        return dynamicInstanceBuilder.build();
    }

    Object[] params(T entity) {
        return paramBuilder.apply(entity);
    }

    private static class ParamField {
        final Field field;
        final String sequence;

        ParamField(Field field, String sequence) {
            this.field = field;
            this.sequence = sequence;
        }
    }
}
