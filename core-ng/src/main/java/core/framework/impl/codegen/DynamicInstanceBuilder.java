package core.framework.impl.codegen;

import core.framework.api.util.Exceptions;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * @author neo
 */
public class DynamicInstanceBuilder<T> {
    private final Logger logger = LoggerFactory.getLogger(DynamicInstanceBuilder.class);
    private final CtClass classBuilder;
    private final ClassPool classPool;
    private Class[] constructorParamClasses;

    public DynamicInstanceBuilder(Class<?> interfaceClass, String className) {
        if (!interfaceClass.isInterface())
            throw Exceptions.error("interface class must be interface, interfaceClass={}", interfaceClass);

        String interfaceClassName = interfaceClass.getCanonicalName();

        classPool = ClassPool.getDefault();
        classBuilder = classPool.makeClass(className);

        try {
            classBuilder.addInterface(classPool.get(interfaceClassName));
            CtConstructor constructor = new CtConstructor(null, classBuilder);
            constructor.setBody(";");
            classBuilder.addConstructor(constructor);
        } catch (NotFoundException | CannotCompileException e) {
            throw new Error(e);
        }
    }

    public DynamicInstanceBuilder<T> constructor(Class[] constructorParamClasses, String body) {
        if (this.constructorParamClasses != null)
            throw new Error("dynamic class must have no more than one custom constructor");

        try {
            this.constructorParamClasses = constructorParamClasses;
            CtClass[] params = new CtClass[constructorParamClasses.length];
            for (int i = 0; i < constructorParamClasses.length; i++) {
                Class<?> paramClass = constructorParamClasses[i];
                params[i] = classPool.getCtClass(paramClass.getCanonicalName());
            }
            CtConstructor constructor = new CtConstructor(params, classBuilder);
            constructor.setBody(body);
            classBuilder.addConstructor(constructor);
            return this;
        } catch (CannotCompileException | NotFoundException e) {
            throw new Error(e);
        }
    }

    public DynamicInstanceBuilder<T> addMethod(String method) {
        try {
            classBuilder.addMethod(CtMethod.make(method, classBuilder));
            return this;
        } catch (CannotCompileException e) {
            logger.info("method:\n{}", method);
            throw new Error(e);
        }
    }

    public DynamicInstanceBuilder<T> addField(String field) {
        try {
            classBuilder.addField(CtField.make(field, classBuilder));
            return this;
        } catch (CannotCompileException e) {
            logger.info("field:\n{}", field);
            throw new Error(e);
        }
    }

    public T build(Object... constructorParams) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> targetClass = classBuilder.toClass();
            classBuilder.detach();
            return targetClass.getDeclaredConstructor(constructorParamClasses).newInstance(constructorParams);
        } catch (CannotCompileException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new Error(e);
        }
    }
}
