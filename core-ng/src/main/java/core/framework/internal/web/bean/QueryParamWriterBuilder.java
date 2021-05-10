package core.framework.internal.web.bean;

import core.framework.api.web.service.QueryParam;
import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.asm.DynamicInstanceBuilder;
import core.framework.internal.reflect.Classes;

import java.lang.reflect.Field;

import static core.framework.internal.asm.Literal.type;

/**
 * @author neo
 */
class QueryParamWriterBuilder<T> {
    private final String helper = type(QueryParamHelper.class);
    private final Class<T> beanClass;
    DynamicInstanceBuilder<QueryParamWriter<T>> builder;

    QueryParamWriterBuilder(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    public QueryParamWriter<T> build() {
        builder = new DynamicInstanceBuilder<>(QueryParamWriter.class, beanClass.getSimpleName());
        builder.addMethod(toParamsMethod());
        return builder.build();
    }

    private String toParamsMethod() {
        CodeBuilder builder = new CodeBuilder().append("public java.util.Map toParams(Object value) {\n");
        builder.indent(1).append("java.util.Map params = new java.util.HashMap();\n");
        builder.indent(1).append("{} bean = ({})value;\n", type(beanClass), type(beanClass));

        for (Field field : Classes.instanceFields(beanClass)) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();
            String name = field.getDeclaredAnnotation(QueryParam.class).name();
            if (String.class.equals(fieldClass)) {
                builder.indent(1).append("params.put(\"{}\", bean.{});\n", name, fieldName);
            } else {
                builder.indent(1).append("params.put(\"{}\", {}.toString(bean.{}));\n", name, helper, fieldName);
            }
        }
        builder.indent(1).append("return params;\n");
        builder.append("}");

        return builder.build();
    }
}
