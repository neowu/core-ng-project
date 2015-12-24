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
final class UpdateQuery<T> {
    private final Function<T, Query> queryBuilder;

    UpdateQuery(Class<T> entityClass) {
        List<Field> primaryKeyFields = Lists.newArrayList();
        List<Field> columnFields = Lists.newArrayList();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                primaryKeyFields.add(field);
            } else if (field.isAnnotationPresent(Column.class)) {
                columnFields.add(field);
            }
        }

        queryBuilder = queryBuilder(entityClass, primaryKeyFields, columnFields);
    }

    private Function<T, Query> queryBuilder(Class<T> entityClass, List<Field> primaryKeyFields, List<Field> columnFields) {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public Object apply(Object value) {\n")
            .indent(1).append("{} entity = ({}) value;\n", entityClass.getCanonicalName(), entityClass.getCanonicalName());

        for (Field primaryKeyField : primaryKeyFields) {
            builder.indent(1).append("if (entity.{} == null) throw new Error(\"primary key must not be null, field={}\");\n", primaryKeyField.getName(), primaryKeyField.getName());
        }
        builder.indent(1).append("StringBuilder sql = new StringBuilder(\"UPDATE {} SET \");\n", entityClass.getDeclaredAnnotation(Table.class).name());
        builder.indent(1).append("java.util.List params = new java.util.ArrayList();\n");
        builder.indent(1).append("int index = 0;\n");

        for (Field field : columnFields) {
            builder.indent(1).append("if (entity.{} != null) {\n", field.getName());
            builder.indent(2).append("if (index > 0) sql.append(\", \");\n");
            builder.indent(2).append("sql.append(\"{} = ?\");\n", field.getDeclaredAnnotation(Column.class).name());
            builder.indent(2).append("params.add(entity.{});\n", field.getName());
            builder.indent(2).append("index++;\n");
            builder.indent(1).append("}\n");
        }

        builder.indent(1).append("sql.append(\"");
        int index = 0;
        for (Field primaryKeyField : primaryKeyFields) {
            if (index == 0) {
                builder.append(" WHERE ");
            } else {
                builder.append(" AND ");
            }
            builder.append("{} = ?", primaryKeyField.getDeclaredAnnotation(Column.class).name());
            index++;
        }
        builder.append("\");\n");

        for (Field primaryKeyField : primaryKeyFields) {
            builder.indent(1).append("params.add(entity.{});\n", primaryKeyField.getName());
        }

        builder.indent(1).append("return new {}(sql.toString(), params.toArray());\n", Query.class.getCanonicalName())
            .append("}");

        return new DynamicInstanceBuilder<Function<T, Query>>(Function.class, UpdateQuery.class.getCanonicalName() + "$" + entityClass.getSimpleName() + "$UpdateQueryBuilder")
            .addMethod(builder.build())
            .build();
    }

    Query query(T entity) {
        return queryBuilder.apply(entity);
    }

    static class Query {
        final String sql;
        final Object[] params;

        Query(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }
    }
}
