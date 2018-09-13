package core.framework.impl.asm;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.concurrent.atomic.AtomicInteger;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class DynamicInstanceBuilder<T> {
    private static final AtomicInteger INDEX = new AtomicInteger();
    private final CtClass classBuilder;
    private final ClassPool classPool;
    private final SourceCode sourceCode = new SourceCode();
    private Class<?>[] constructorParamClasses;

    public DynamicInstanceBuilder(Class<? super T> interfaceClass, String className) {
        if (!interfaceClass.isInterface())
            throw new Error(format("class must be interface, class={}", interfaceClass.getCanonicalName()));

        sourceCode.interfaceClass = interfaceClass;
        sourceCode.className = className;

        classPool = ClassPool.getDefault();
        classBuilder = classPool.makeClass(className + "$" + (INDEX.getAndIncrement()));

        try {
            classBuilder.addInterface(classPool.get(interfaceClass.getName()));
            CtConstructor constructor = new CtConstructor(null, classBuilder);
            constructor.setBody(";");
            classBuilder.addConstructor(constructor);
        } catch (NotFoundException | CannotCompileException e) {
            throw new Error(e);
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
            throw new Error(format("{}, source:\n{}", e.getMessage(), body), e);
        }
    }

    public void addMethod(String method) {
        sourceCode.methods.add(method);
        try {
            classBuilder.addMethod(CtMethod.make(method, classBuilder));
        } catch (CannotCompileException e) {
            throw new Error(format("{}, source:\n{}", e.getMessage(), method), e);
        }
    }

    public void addField(String pattern, Object... argument) {
        String field = format(pattern, argument);
        sourceCode.fields.add(field);
        try {
            classBuilder.addField(CtField.make(field, classBuilder));
        } catch (CannotCompileException e) {
            throw new Error(format("{}, source:\n{}", e.getMessage(), field), e);
        }
    }

    public T build(Object... constructorParams) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> targetClass = (Class<T>) classBuilder.toClass();
            classBuilder.detach();
            return targetClass.getDeclaredConstructor(constructorParamClasses).newInstance(constructorParams);
        } catch (CannotCompileException | ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public String sourceCode() {
        return sourceCode.build();
    }
}
