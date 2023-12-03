package test;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class ListenerPriorityTest {
    @Test
    public void executeTest() {
        EventBus eventBus = EventBus.threadSafe();
        eventBus.registerLambdaFactory("test", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        eventBus.subscribe(this);

        TestPriorityEvent event = new TestPriorityEvent();
        eventBus.post(event);

        Assertions.assertArrayEquals(
                event.priorities.toArray(new Integer[0]),
                new Integer[]{EventPriority.HIGHEST, EventPriority.HIGH, EventPriority.MEDIUM, EventPriority.LOW, EventPriority.LOWEST}
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEventHighest(TestPriorityEvent event) {
        event.priorities.add(EventPriority.HIGHEST);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onEventHigh(TestPriorityEvent event) {
        event.priorities.add(EventPriority.HIGH);
    }

    @EventHandler(priority = EventPriority.MEDIUM)
    private void onEventMedium(TestPriorityEvent event) {
        event.priorities.add(EventPriority.MEDIUM);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEventLow(TestPriorityEvent event) {
        event.priorities.add(EventPriority.LOW);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onEventLowest(TestPriorityEvent event) {
        event.priorities.add(EventPriority.LOWEST);
    }

    private static class TestPriorityEvent {
        public final List<Integer> priorities = new ArrayList<>();
    }
}
