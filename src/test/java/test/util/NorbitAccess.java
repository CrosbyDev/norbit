package test.util;

import java.lang.invoke.MethodHandles;

/**
 * Deep reflection into norbit, for use in unit testing
 * @author Crosby
 */
public class NorbitAccess {
    private static LookupFactoryImplType cachedImplType = null;

    /**
     * @return the {@link LookupFactoryImplType} currently in use in {@link io.github.racoondog.norbit.listeners.LambdaListener}
     */
    public static LookupFactoryImplType getCurrentImplType() {
        if (cachedImplType != null) return cachedImplType;
        return cachedImplType = findImplType();
    }

    private static LookupFactoryImplType findImplType() {
        try {
            MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            return LookupFactoryImplType.SCOPED_LOOKUP;
        } catch (Exception e) {
            if (e instanceof SecurityException) return LookupFactoryImplType.PUBLIC_LOOKUP;

            try {
                MethodHandles.Lookup.class.getDeclaredConstructor(Class.class).setAccessible(true);
                return LookupFactoryImplType.LEGACY_PRIVATE_LOOKUP;
            } catch (NoSuchMethodException ignored) {}
        }
        return LookupFactoryImplType.PUBLIC_LOOKUP;
    }

    public enum LookupFactoryImplType {
        SCOPED_LOOKUP(true),
        LEGACY_PRIVATE_LOOKUP(false),
        PUBLIC_LOOKUP(false);

        public final boolean requireLambdaFactory;

        LookupFactoryImplType(boolean requireLambdaFactory) {
            this.requireLambdaFactory = requireLambdaFactory;
        }
    }
}
