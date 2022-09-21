package core.framework.internal.db;

import core.framework.db.Column;
import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.asm.DynamicInstanceBuilder;
import core.framework.internal.reflect.Classes;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static core.framework.internal.asm.Literal.type;
import static core.framework.internal.asm.Literal.variable;

/**
 * @author neo
 */
final class RowMapperBuilder<T> {
    final DynamicInstanceBuilder<RowMapper<T>> builder;
    private final Class<T> entityClass;
    private final EnumDBMapper enumDBMapper;

    RowMapperBuilder(Class<T> entityClass, EnumDBMapper enumDBMapper) {
        this.entityClass = entityClass;
        this.enumDBMapper = enumDBMapper;
        builder = new DynamicInstanceBuilder<>(RowMapper.class, entityClass.getSimpleName());
    }

    RowMapper<T> build() {
        builder.addMethod(mapMethod());
        return builder.build();
    }

    private String mapMethod() {
        var builder = new CodeBuilder().append("public Object map({} resultSet) {\n", type(ResultSetWrapper.class));
        String entityClassLiteral = type(entityClass);
        builder.indent(1).append("{} entity = new {}();\n", entityClassLiteral, entityClassLiteral);

        for (Field field : Classes.instanceFields(entityClass)) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();
            Column column = field.getDeclaredAnnotation(Column.class);
            String columnName = column.name();

            if (Integer.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getInt(\"{}\");\n", fieldName, columnName);
            } else if (String.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getString(\"{}\");\n", fieldName, columnName);
            } else if (Boolean.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getBoolean(\"{}\");\n", fieldName, columnName);
            } else if (Long.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getLong(\"{}\");\n", fieldName, columnName);
            } else if (LocalDateTime.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getLocalDateTime(\"{}\");\n", fieldName, columnName);
            } else if (LocalDate.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getLocalDate(\"{}\");\n", fieldName, columnName);
            } else if (ZonedDateTime.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getZonedDateTime(\"{}\");\n", fieldName, columnName);
            } else if (fieldClass.isEnum()) {
                registerEnumClass(fieldClass);
                this.builder.addField("private final {} {}Mappings = new {}({});", type(DBEnumMapper.class), fieldName, type(DBEnumMapper.class), variable(fieldClass));
                builder.indent(1).append("entity.{} = ({}){}Mappings.getEnum(resultSet.getString(\"{}\"));\n", fieldName, type(fieldClass), fieldName, columnName);
            } else if (Double.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getDouble(\"{}\");\n", fieldName, columnName);
            } else if (BigDecimal.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getBigDecimal(\"{}\");\n", fieldName, columnName);
            } else if (column.json()) {
                builder.indent(1).append("entity.{} = ({}) {}.fromJSON(resultSet.getString(\"{}\"), {});\n", fieldName, type(field.getType()), type(JSONHelper.class), columnName, variable(field.getGenericType()));
            }
        }
        builder.indent(1).append("return entity;\n");
        builder.append("}");

        return builder.build();
    }

    private void registerEnumClass(Class<?> fieldClass) {
        @SuppressWarnings("unchecked")
        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) fieldClass;
        enumDBMapper.registerEnumClass(enumClass);
    }
}
