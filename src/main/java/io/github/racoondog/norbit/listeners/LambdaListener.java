package io.github.racoondog.norbit.listeners;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.listeners.IListener;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
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
    private static final LookupFactory LOOKUP_FACTORY;
    private static final Map<Method, MethodHandle> methodHandleCache = new ConcurrentHashMap<>();

    private final Class<?> target;
    private final boolean isStatic;
    private final int priority;
    private final Consumer<Object> executor;
    public final Class<?> owner;

    /**
     * Creates a new lambda listener, can be used for both static and non-static methods.
     * @param klass Class of the object
     * @param object Object, null if static
     * @param method Method to create lambda for
     */
    @SuppressWarnings("unchecked")
    public LambdaListener(meteordevelopment.orbit.listeners.LambdaListener.Factory factory, Class<?> klass, Object object, Method method) {
        this.target = method.getParameters()[0].getType();
        this.isStatic = Modifier.isStatic(method.getModifiers());
        this.priority = method.getAnnotation(EventHandler.class).priority();
        this.owner = klass;

        try {
            this.executor = isStatic ? (Consumer<Object>) staticLambdaFactory(factory, klass, method).invoke()
                    : (Consumer<Object>) instanceLambdaFactory(factory, klass, method).invoke(object);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static MethodHandle staticLambdaFactory(meteordevelopment.orbit.listeners.LambdaListener.Factory factory, Class<?> klass, Method method) throws Throwable {
        String name = method.getName();
        MethodHandles.Lookup lookup = LOOKUP_FACTORY.of(factory, klass);

        MethodType methodType = MethodType.methodType(void.class, method.getParameters()[0].getType());

        MethodHandle methodHandle = lookup.findStatic(klass, name, methodType);
        MethodType invokedType = MethodType.methodType(Consumer.class);

        return LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType(void.class, Object.class), methodHandle, methodType).getTarget();
    }

    private static MethodHandle instanceLambdaFactory(meteordevelopment.orbit.listeners.LambdaListener.Factory factory, Class<?> klass, Method method) throws Throwable {
        MethodHandle lambdaFactory = methodHandleCache.get(method);
        if (lambdaFactory != null) return lambdaFactory;

        String name = method.getName();
        MethodHandles.Lookup lookup = LOOKUP_FACTORY.of(factory, klass);

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

    /**
     * @return whether the current {@link LookupFactory} implementation requires lambda factory registration.
     */
    public static boolean requireLambdaFactoryRegistration() {
        return LOOKUP_FACTORY.requireLambdaFactory;
    }

    private static abstract class LookupFactory {
        public final boolean requireLambdaFactory;

        public LookupFactory(boolean requireLambdaFactory) {
            this.requireLambdaFactory = requireLambdaFactory;
        }

        public abstract MethodHandles.Lookup of(meteordevelopment.orbit.listeners.LambdaListener.Factory lambdaFactory, Class<?> klass) throws InvocationTargetException, IllegalAccessException, InstantiationException;
    }

    static {
        LookupFactory lookupFactory = null;

        try {
            try {
                // modern implementation (java 9+)
                Method privateLookupInMethod = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
                lookupFactory = new LookupFactory(true) {
                    @Override
                    public MethodHandles.Lookup of(meteordevelopment.orbit.listeners.LambdaListener.Factory lambdaFactory, Class<?> klass) throws InvocationTargetException, IllegalAccessException {
                        return lambdaFactory.create(privateLookupInMethod, klass);
                    }
                };
            } catch (Exception e) {
                if (e instanceof SecurityException se) {
                    // rethrow to catch later
                    throw se;
                }

                try {
                    // legacy implementation (java 8)
                    Constructor<MethodHandles.Lookup> lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
                    lookupConstructor.setAccessible(true);
                    lookupFactory = new LookupFactory(false) {
                        @Override
                        public MethodHandles.Lookup of(meteordevelopment.orbit.listeners.LambdaListener.Factory lambdaFactory, Class<?> klass) throws InvocationTargetException, IllegalAccessException, InstantiationException {
                            return lookupConstructor.newInstance(klass);
                        }
                    };
                } catch (NoSuchMethodException ex) {
                    System.err.println("Could not find lookup constructor, using public-only fallback. Details: " + ex.getLocalizedMessage());
                }
            }
        } catch (SecurityException e) {
            System.err.println("Reflection-based implementation blocked by security manager, using public-only fallback. Details: " + e.getLocalizedMessage());
        }

        if (lookupFactory == null) {
            // fallback implementation when deep reflection is prevented, only functions with public event listener methods
            lookupFactory = new LookupFactory(false) {
                @Override
                public MethodHandles.Lookup of(meteordevelopment.orbit.listeners.LambdaListener.Factory lambdaFactory, Class<?> klass) {
                    return MethodHandles.publicLookup().in(klass);
                }
            };
        }

        LOOKUP_FACTORY = lookupFactory;
    }
}
