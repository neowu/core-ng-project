package core.framework.impl.asm;

import core.framework.util.Lists;

import java.util.List;

import static core.framework.impl.asm.Literal.type;

/**
 * @author neo
 */
final class SourceCode {
    final List<String> fields = Lists.newArrayList();
    final List<String> methods = Lists.newArrayList();
    Class<?> interfaceClass;
    String className;
    Class<?>[] constructorParamClasses;
    String constructorBody;

    public String build() {
        CodeBuilder builder = new CodeBuilder();
        String classSimpleName = className.substring(className.lastIndexOf('.') + 1);
        builder.append("public class {} implements {} {\n", classSimpleName, type(interfaceClass));
        for (String field : fields) {
            builder.indent(1).append(field);
            builder.append("\n\n");
        }
        if (constructorParamClasses != null) {
            buildConstructor(builder, classSimpleName);
            builder.append("\n\n");
        }
        for (String method : methods) {
            addMethod(builder, method);
            builder.append("\n\n");
        }
        builder.append("}\n");
        return builder.build();
    }

    private void addMethod(CodeBuilder builder, String method) {
        builder.append("    ");
        int length = method.length();
        for (int i = 0; i < length; i++) {
            char ch = method.charAt(i);
            if (ch == '\n') builder.append("\n    "); // indent method
            else builder.append(ch);
        }
    }

    private void buildConstructor(CodeBuilder builder, String classSimpleName) {
        builder.indent(1).append("public {}(", classSimpleName);
        for (int i = 0; i < constructorParamClasses.length; i++) {
            Class<?> paramClass = constructorParamClasses[i];
            if (i > 0) builder.append(", ");
            builder.append("{} ${}", type(paramClass), i + 1);
        }
        builder.append(") {\n");
        builder.indent(2).append(constructorBody).append("\n")
               .indent(1).append('}');
    }
}
