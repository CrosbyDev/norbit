package io.github.racoondog.util;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.listeners.IListener;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * Norbit includes some extra sanity checks that change the method semantics compared to the base orbit implementation.
 * This class is used to bypass those checks and compare the performance of orbit and norbit on an equal playing field.
 */
public class NorbitAccess {
    private static final MethodHandle LISTENER_SUBSCRIBE;

    public static void subscribe(EventBus eventBus, IListener listener, boolean check) {
        try {
            LISTENER_SUBSCRIBE.invokeExact(eventBus, listener, check);
        } catch (Throwable ignored) {}
    }

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            MethodHandles.lookup();
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long lookupFieldOffset = unsafe.staticFieldOffset(lookupField);
            MethodHandles.Lookup trusted = (MethodHandles.Lookup) unsafe.getObject(MethodHandles.Lookup.class, lookupFieldOffset);

            LISTENER_SUBSCRIBE = trusted.findVirtual(EventBus.class, "subscribe", MethodType.methodType(void.class, IListener.class, boolean.class));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
