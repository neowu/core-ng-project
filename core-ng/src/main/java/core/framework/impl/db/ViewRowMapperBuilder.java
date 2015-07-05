package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.RowMapper;
import core.framework.impl.codegen.CodeBuilder;
import core.framework.impl.codegen.DynamicInstanceBuilder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author neo
 */
public class ViewRowMapperBuilder<T> {
    private final Class<T> entityClass;

    public ViewRowMapperBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public RowMapper<T> build() {
        DynamicInstanceBuilder<RowMapper<T>> builder = new DynamicInstanceBuilder<>(RowMapper.class, RowMapper.class.getCanonicalName() + "$" + entityClass.getCanonicalName());
        builder.addMethod(mapMethod());
        return builder.build();
    }

    private String mapMethod() {
        String entityClassName = entityClass.getCanonicalName();

        CodeBuilder builder = new CodeBuilder().append("public Object map(core.framework.api.db.Row row) {\n");
        builder.indent(1).append("{} entity = new {}();\n", entityClassName, entityClassName);

        for (Field field : entityClass.getFields()) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();
            String column = field.getAnnotation(Column.class).name();
            if (Integer.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = row.getInt(\"{}\");\n", fieldName, column);
            } else if (String.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = row.getString(\"{}\");\n", fieldName, column);
            } else if (Boolean.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = row.getBoolean(\"{}\");\n", fieldName, column);
            } else if (Long.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = row.getLong(\"{}\");\n", fieldName, column);
            } else if (LocalDateTime.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = row.getLocalDateTime(\"{}\");\n", fieldName, column);
            } else if (Enum.class.isAssignableFrom(fieldClass)) {
                builder.indent(1).append("entity.{} = ({}) row.getEnum(\"{}\", {}.class);\n", fieldName, fieldClass.getCanonicalName(), column, fieldClass.getCanonicalName());
            } else if (Double.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = row.getDouble(\"{}\");\n", fieldName, column);
            } else if (BigDecimal.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = row.getBigDecimal(\"{}\");\n", fieldName, column);
            }
        }
        builder.indent(1).append("return entity;\n");
        builder.append("}");

        return builder.toString();
    }
}
