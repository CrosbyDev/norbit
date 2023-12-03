package test;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

public class StaticListenerTest {
    @Test
    public void executeTest() {
        EventBus eventBus = EventBus.threadSafe();
        eventBus.registerLambdaFactory("test", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        eventBus.subscribe(StaticListenerTest.class);

        TestEvent event = new TestEvent();
        eventBus.post(event);

        Assertions.assertTrue(event.wasRan());
    }

    @EventHandler
    public void onEventNonStatic(TestEvent event) {
        Assertions.fail();
    }

    @EventHandler
    public static void onEventStatic(TestEvent event) {
        event.callback();
    }
}
