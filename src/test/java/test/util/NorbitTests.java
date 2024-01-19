package test.util;

import io.github.racoondog.norbit.EventBus;

import java.lang.invoke.MethodHandles;

public class NorbitTests {
    public static EventBus create() {
        EventBus eventBus = EventBus.threadSafe();
        eventBus.registerLambdaFactory("test", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        return eventBus;
    }
}
