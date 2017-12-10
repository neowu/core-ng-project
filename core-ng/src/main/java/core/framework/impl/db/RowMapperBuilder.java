package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static core.framework.impl.asm.Literal.type;
import static core.framework.impl.asm.Literal.variable;

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
        builder = new DynamicInstanceBuilder<>(RowMapper.class, RowMapper.class.getCanonicalName() + "$" + entityClass.getSimpleName());
    }

    RowMapper<T> build() {
        builder.addMethod(mapMethod());
        return builder.build();
    }

    private String mapMethod() {
        CodeBuilder builder = new CodeBuilder().append("public Object map({} resultSet) {\n", type(ResultSetWrapper.class));
        String entityClassLiteral = type(entityClass);
        builder.indent(1).append("{} entity = new {}();\n", entityClassLiteral, entityClassLiteral);

        for (Field field : Classes.instanceFields(entityClass)) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();
            String column = field.getDeclaredAnnotation(Column.class).name();
            if (Integer.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getInt(\"{}\");\n", fieldName, column);
            } else if (String.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getString(\"{}\");\n", fieldName, column);
            } else if (Boolean.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getBoolean(\"{}\");\n", fieldName, column);
            } else if (Long.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getLong(\"{}\");\n", fieldName, column);
            } else if (LocalDateTime.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getLocalDateTime(\"{}\");\n", fieldName, column);
            } else if (LocalDate.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getLocalDate(\"{}\");\n", fieldName, column);
            } else if (ZonedDateTime.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getZonedDateTime(\"{}\");\n", fieldName, column);
            } else if (fieldClass.isEnum()) {
                registerEnumClass(fieldClass);
                this.builder.addField("private final {} {}Mappings = new {}({});", type(DBEnumMapper.class), fieldName, type(DBEnumMapper.class), variable(fieldClass));
                builder.indent(1).append("entity.{} = ({}){}Mappings.getEnum(resultSet.getString(\"{}\"));\n", fieldName, type(fieldClass), fieldName, column);
            } else if (Double.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getDouble(\"{}\");\n", fieldName, column);
            } else if (BigDecimal.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getBigDecimal(\"{}\");\n", fieldName, column);
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
