package test;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.util.NorbitTests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListenerPriorityTest {
    @Test
    public void executeTest() {
        NorbitTests.test(eventBus -> {
            eventBus.subscribe(this);

            TestPriorityEvent event = new TestPriorityEvent();
            eventBus.post(event);

            Assertions.assertTrue(
                    NorbitTests.isPrioritySorted(event.priorities),
                    () -> "Listener were ran out of order: " + event.priorities.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"))
            );
        }).withInitialization().ofEvent(TestPriorityEvent.class).execute();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEventHighest(TestPriorityEvent event) {
        event.priorities.add(EventPriority.HIGHEST);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onEventHigh(TestPriorityEvent event) {
        event.priorities.add(EventPriority.HIGH);
    }

    @SuppressWarnings("DefaultAnnotationParam")
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
