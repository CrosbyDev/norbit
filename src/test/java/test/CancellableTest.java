package test;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.util.NorbitTests;
import test.util.TestCancellableEvent;

public class CancellableTest {
    @Test
    public void executeTest() {
        NorbitTests.test(eventBus -> {
            eventBus.subscribe(this);

            eventBus.post(new TestCancellableEvent());
        }).withInitialization().ofEvent(TestCancellableEvent.class).execute();
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
