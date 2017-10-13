package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.util.Lists;

import java.lang.reflect.Field;
import java.util.List;

import static core.framework.impl.asm.Literal.type;

/**
 * @author neo
 */
class UpdateQueryBuilder<T> {
    final DynamicInstanceBuilder<UpdateQuery<T>> builder;
    private final Class<T> entityClass;

    UpdateQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        builder = new DynamicInstanceBuilder<>(UpdateQuery.class, UpdateQueryBuilder.class.getCanonicalName() + "$" + entityClass.getSimpleName());
    }

    UpdateQuery<T> build() {
        List<Field> primaryKeyFields = Lists.newArrayList();
        List<Field> columnFields = Lists.newArrayList();
        for (Field field : Classes.instanceFields(entityClass)) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                primaryKeyFields.add(field);
            } else if (field.isAnnotationPresent(Column.class)) {
                columnFields.add(field);
            }
        }

        builder.addMethod(updateMethod(entityClass, primaryKeyFields, columnFields));
        return builder.build();
    }

    private String updateMethod(Class<T> entityClass, List<Field> primaryKeyFields, List<Field> columnFields) {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public {} update(Object value) {\n", type(UpdateQuery.Statement.class))
               .indent(1).append("{} entity = ({}) value;\n", type(entityClass), type(entityClass));

        for (Field primaryKeyField : primaryKeyFields) {
            builder.indent(1).append("if (entity.{} == null) throw new Error(\"primary key must not be null, field={}\");\n", primaryKeyField.getName(), primaryKeyField.getName());
        }
        builder.indent(1).append("StringBuilder sql = new StringBuilder(\"UPDATE {} SET \");\n", entityClass.getDeclaredAnnotation(Table.class).name());
        builder.indent(1).append("java.util.List params = new java.util.ArrayList();\n");
        builder.indent(1).append("int index = 0;\n");

        for (Field field : columnFields) {
            builder.indent(1).append("if (entity.{} != null) {\n", field.getName())
                   .indent(2).append("if (index > 0) sql.append(\", \");\n")
                   .indent(2).append("sql.append(\"{} = ?\");\n", field.getDeclaredAnnotation(Column.class).name())
                   .indent(2).append("params.add(entity.{});\n", field.getName())
                   .indent(2).append("index++;\n")
                   .indent(1).append("}\n");
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

        builder.indent(1).append("return new {}(sql.toString(), params.toArray());\n", type(UpdateQuery.Statement.class))
               .append("}");
        return builder.build();
    }
}
