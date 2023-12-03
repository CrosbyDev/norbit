package test;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

public class CancellableTest {
    @Test
    public void executeTest() {
        EventBus eventBus = EventBus.threadSafe();
        eventBus.registerLambdaFactory("test", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        eventBus.subscribe(this);

        eventBus.post(new TestCancellableEvent());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onEventBefore(TestCancellableEvent event) {
        event.cancel();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEventAfter(TestCancellableEvent event) {
        Assertions.fail("Ran cancelled event.");
    }
}
