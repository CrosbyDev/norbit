package io.github.racoondog.norbit.listeners;

import io.github.racoondog.norbit.EventHandler;

import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Default implementation of a {@link IListener} that creates a lambda at runtime to call the target method.
 */
public class LambdaListener implements IListener {
    @FunctionalInterface
    public interface Factory {
        MethodHandles.Lookup create(Method lookupInMethod, Class<?> klass) throws InvocationTargetException, IllegalAccessException;
    }

    private static final Method privateLookupInMethod;
    public static final Map<Method, MethodHandle> methodHandleCache = new ConcurrentHashMap<>();

    private final Class<?> target;
    private final boolean isStatic;
    private final int priority;
    private final Consumer<Object> executor;

    /**
     * Creates a new lambda listener, can be used for both static and non-static methods.
     * @param klass Class of the object
     * @param object Object, null if static
     * @param method Method to create lambda for
     */
    @SuppressWarnings("unchecked")
    public LambdaListener(Factory factory, Class<?> klass, Object object, Method method) {
        this.target = method.getParameters()[0].getType();
        this.isStatic = Modifier.isStatic(method.getModifiers());
        this.priority = method.getAnnotation(EventHandler.class).priority();

        try {
            this.executor = isStatic ? (Consumer<Object>) staticLambdaFactory(factory, klass, method).invoke()
                    : (Consumer<Object>) instanceLambdaFactory(factory, klass, method).invoke(object);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static MethodHandle staticLambdaFactory(Factory factory, Class<?> klass, Method method) throws Throwable {
        String name = method.getName();
        MethodHandles.Lookup lookup = factory.create(privateLookupInMethod, klass);

        MethodType methodType = MethodType.methodType(void.class, method.getParameters()[0].getType());

        MethodHandle methodHandle = lookup.findStatic(klass, name, methodType);
        MethodType invokedType = MethodType.methodType(Consumer.class);

        return LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType(void.class, Object.class), methodHandle, methodType).getTarget();
    }

    private static MethodHandle instanceLambdaFactory(Factory factory, Class<?> klass, Method method) throws Throwable {
        MethodHandle lambdaFactory = methodHandleCache.get(method);
        if (lambdaFactory != null) return lambdaFactory;

        String name = method.getName();
        MethodHandles.Lookup lookup = factory.create(privateLookupInMethod, klass);

        MethodType methodType = MethodType.methodType(void.class, method.getParameters()[0].getType());

        MethodHandle methodHandle = lookup.findVirtual(klass, name, methodType);
        MethodType invokedType = MethodType.methodType(Consumer.class, klass);

        lambdaFactory = LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType(void.class, Object.class), methodHandle, methodType).getTarget();
        methodHandleCache.put(method, lambdaFactory);
        return lambdaFactory;
    }

    @Override
    public void call(Object event) {
        executor.accept(event);
    }

    @Override
    public Class<?> getTarget() {
        return target;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    static {
        try {
            privateLookupInMethod = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
