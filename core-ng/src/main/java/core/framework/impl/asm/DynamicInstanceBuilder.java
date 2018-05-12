package core.framework.impl.asm;

import core.framework.util.Exceptions;
import core.framework.util.Strings;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author neo
 */
public class DynamicInstanceBuilder<T> {
    private static final AtomicInteger INDEX = new AtomicInteger();
    private final Logger logger = LoggerFactory.getLogger(DynamicInstanceBuilder.class);
    private final CtClass classBuilder;
    private final ClassPool classPool;
    private final SourceCode sourceCode = new SourceCode();
    private Class<?>[] constructorParamClasses;

    public DynamicInstanceBuilder(Class<?> interfaceClass, String className) {
        if (!interfaceClass.isInterface())
            throw Exceptions.error("interface class must be interface, interfaceClass={}", interfaceClass);

        sourceCode.interfaceClass = interfaceClass;
        sourceCode.className = className;

        classPool = ClassPool.getDefault();
        classBuilder = classPool.makeClass(className + "$" + (INDEX.getAndIncrement()));

        try {
            classBuilder.addInterface(classPool.get(interfaceClass.getCanonicalName()));
            CtConstructor constructor = new CtConstructor(null, classBuilder);
            constructor.setBody(";");
            classBuilder.addConstructor(constructor);
        } catch (NotFoundException | CannotCompileException e) {
            throw new CodeCompileException(e);
        }
    }

    public void constructor(Class<?>[] constructorParamClasses, String body) {
        if (this.constructorParamClasses != null)
            throw new Error("dynamic class must have no more than one custom constructor");

        sourceCode.constructorParamClasses = constructorParamClasses;
        sourceCode.constructorBody = body;

        try {
            this.constructorParamClasses = constructorParamClasses;
            CtClass[] params = new CtClass[constructorParamClasses.length];
            for (int i = 0; i < constructorParamClasses.length; i++) {
                Class<?> paramClass = constructorParamClasses[i];
                params[i] = classPool.getCtClass(paramClass.getName());
            }
            CtConstructor constructor = new CtConstructor(params, classBuilder);
            constructor.setBody(body);
            classBuilder.addConstructor(constructor);
        } catch (CannotCompileException | NotFoundException e) {
            logger.error("constructor body failed to compile:\n{}", body);
            throw new CodeCompileException(e);
        }
    }

    public void addMethod(String method) {
        sourceCode.methods.add(method);
        try {
            classBuilder.addMethod(CtMethod.make(method, classBuilder));
        } catch (CannotCompileException e) {
            logger.error("method failed to compile:\n{}", method);
            throw new CodeCompileException(e);
        }
    }

    public void addField(String pattern, Object... argument) {
        String field = Strings.format(pattern, argument);
        sourceCode.fields.add(field);
        try {
            classBuilder.addField(CtField.make(field, classBuilder));
        } catch (CannotCompileException e) {
            logger.error("field failed to compile:\n{}", field);
            throw new CodeCompileException(e);
        }
    }

    public T build(Object... constructorParams) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> targetClass = classBuilder.toClass();
            classBuilder.detach();
            return targetClass.getDeclaredConstructor(constructorParamClasses).newInstance(constructorParams);
        } catch (CannotCompileException | ReflectiveOperationException e) {
            throw new CodeCompileException(e);
        }
    }

    public String sourceCode() {
        return sourceCode.build();
    }
}
